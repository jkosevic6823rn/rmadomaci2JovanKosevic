package rs.edu.raf.rma.showtime.quiz.data

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlin.time.Clock
import rs.edu.raf.rma.movie.data.network.MovieService
import rs.edu.raf.rma.showtime.quiz.data.db.QuizDao
import rs.edu.raf.rma.showtime.quiz.data.db.QuizMovieEntity
import rs.edu.raf.rma.showtime.quiz.data.db.QuizResultEntity
import rs.edu.raf.rma.showtime.quiz.data.network.PostQuizRequest
import rs.edu.raf.rma.showtime.quiz.data.network.QuizApi
import rs.edu.raf.rma.showtime.quiz.domain.QuizGenerator
import rs.edu.raf.rma.showtime.quiz.domain.QuizMovie
import rs.edu.raf.rma.showtime.quiz.domain.QuizQuestion
import rs.edu.raf.rma.showtime.quiz.domain.QuizScoring

class QuizRepository(
    private val movieService: MovieService,
    private val quizApi: QuizApi,
    private val dao: QuizDao,
    private val generator: QuizGenerator,
) {

    /** Puni lokalni pool top filmovima ako je premali (Room kao SSOT za kviz). */
    suspend fun ensurePool() {
        if (dao.countMoviesWithPoster() >= MIN_POOL_SIZE) return
        val response = movieService.getMovies(pageSize = BOOTSTRAP_SIZE, sortBy = "imdb_rating")
        dao.upsertMovies(
            response.items.map {
                QuizMovieEntity(
                    imdbId = it.imdbId,
                    title = it.title,
                    year = it.year,
                    posterPath = it.posterPath,
                )
            },
        )
    }

    suspend fun canStartQuiz(): Boolean = dao.countMoviesWithPoster() >= MIN_POOL_SIZE

    /**
     * Priprema sesiju od [QuizScoring.TOTAL_QUESTIONS] pitanja. Obogaćuje filmove
     * (backdrop + glumci) u serijama dok generator ne sastavi dovoljno pitanja.
     */
    suspend fun buildSession(): List<QuizQuestion> = coroutineScope {
        val candidates = dao.moviesWithPoster().shuffled()
        val enriched = mutableListOf<QuizMovie>()
        var offset = 0
        while (offset < candidates.size) {
            val batch = candidates.subList(offset, minOf(offset + ENRICH_BATCH, candidates.size))
            offset += ENRICH_BATCH

            val enrichedBatch = batch.map { async { enrich(it) } }.awaitAll()
            enriched += enrichedBatch

            val questions = generator.generate(enriched)
            if (questions.size >= QuizScoring.TOTAL_QUESTIONS) return@coroutineScope questions
        }
        generator.generate(enriched)
    }

    private suspend fun enrich(entity: QuizMovieEntity): QuizMovie {
        var backdrop = entity.backdropPath
        var actors = entity.actors?.split(ACTOR_SEPARATOR)?.filter { it.isNotBlank() } ?: emptyList()

        if (backdrop == null) {
            backdrop = runCatching {
                movieService.getImages(entity.imdbId, type = "backdrop").backdrops.firstOrNull()?.filePath
            }.getOrNull()
        }
        if (actors.isEmpty()) {
            actors = runCatching {
                movieService.getCast(entity.imdbId, pageSize = 5).items.map { it.name }
            }.getOrDefault(emptyList())
        }

        val actorsCsv = actors.joinToString(ACTOR_SEPARATOR).ifBlank { null }
        if (backdrop != entity.backdropPath || actorsCsv != entity.actors) {
            runCatching { dao.updateEnrichment(entity.imdbId, backdrop, actorsCsv) }
        }

        return QuizMovie(
            imdbId = entity.imdbId,
            title = entity.title,
            year = entity.year,
            posterPath = entity.posterPath,
            backdropPath = backdrop,
            actors = actors,
        )
    }

    /** Čuva rezultat lokalno (uvek), pa pokušava da ga pošalje na leaderboard. */
    suspend fun submitResult(score: Double, correctCount: Int) {
        dao.insertResult(
            QuizResultEntity(
                score = score,
                correctCount = correctCount,
                playedAt = Clock.System.now().toEpochMilliseconds(),
            ),
        )
        runCatching { quizApi.postQuizResult(PostQuizRequest(score = score, category = 1)) }
    }

    fun observeBestScore(): Flow<Double?> = dao.observeBestScore()
    fun observeGamesPlayed(): Flow<Int> = dao.observeGamesPlayed()

    companion object {
        const val MIN_POOL_SIZE = 10
        const val BOOTSTRAP_SIZE = 100
        const val ENRICH_BATCH = 16
        const val ACTOR_SEPARATOR = "|"
    }
}
