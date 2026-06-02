package rs.edu.raf.rma.showtime.quiz.domain

import kotlin.random.Random

/**
 * Generiše kviz pitanja iz obogaćenih filmova. Čista logika (bez mreže/IO),
 * pa je laka za testiranje.
 *
 * Pravila iz specifikacije:
 *  - tipovi se randomizuju; nijedan tip ne više od [MAX_PER_TYPE] puta po sesiji,
 *  - nema ponavljanja slika u okviru sesije,
 *  - svaki lažni odgovor je različit,
 *  - ako film ne ispunjava uslove tipa, preskače se (rad stabilno završava).
 */
class QuizGenerator(private val random: Random = Random.Default) {

    fun generate(
        movies: List<QuizMovie>,
        count: Int = QuizScoring.TOTAL_QUESTIONS,
    ): List<QuizQuestion> {
        val questions = mutableListOf<QuizQuestion>()
        val usedImages = mutableSetOf<String>()
        val typeCount = mutableMapOf<QuestionType, Int>()

        val allTitles = movies.map { it.title }.filter { it.isNotBlank() }.distinct()
        val allActors = movies.flatMap { it.actors }.map { it.trim() }.filter { it.isNotBlank() }.distinct()

        for (movie in movies.shuffled(random)) {
            if (questions.size >= count) break

            val candidateTypes = supportedTypes(movie, allTitles, allActors)
                .filter { (typeCount[it] ?: 0) < MAX_PER_TYPE }
                .shuffled(random)

            for (type in candidateTypes) {
                val question = when (type) {
                    QuestionType.GUESS_MOVIE -> buildGuessMovie(movie, allTitles, usedImages)
                    QuestionType.GUESS_YEAR -> buildGuessYear(movie, usedImages)
                    QuestionType.GUESS_ACTOR -> buildGuessActor(movie, allActors, usedImages)
                }
                if (question != null) {
                    questions += question
                    typeCount[type] = (typeCount[type] ?: 0) + 1
                    break
                }
            }
        }
        return questions
    }

    private fun supportedTypes(
        movie: QuizMovie,
        allTitles: List<String>,
        allActors: List<String>,
    ): List<QuestionType> = buildList {
        if (movie.backdropPath != null && allTitles.count { it != movie.title } >= OPTIONS - 1) {
            add(QuestionType.GUESS_MOVIE)
        }
        if (movie.posterPath != null && movie.year != null) {
            add(QuestionType.GUESS_YEAR)
        }
        if (movie.posterPath != null && movie.actors.isNotEmpty() &&
            allActors.count { it !in movie.actors } >= OPTIONS - 1
        ) {
            add(QuestionType.GUESS_ACTOR)
        }
    }

    private fun buildGuessMovie(
        movie: QuizMovie,
        allTitles: List<String>,
        usedImages: MutableSet<String>,
    ): QuizQuestion? {
        val image = TmdbImage.backdrop(movie.backdropPath) ?: return null
        if (image in usedImages) return null
        val distractors = allTitles.filter { it != movie.title }.shuffled(random).take(OPTIONS - 1)
        if (distractors.size < OPTIONS - 1) return null
        usedImages += image
        return makeQuestion(QuestionType.GUESS_MOVIE, image, title = null, correct = movie.title, distractors)
    }

    private fun buildGuessYear(
        movie: QuizMovie,
        usedImages: MutableSet<String>,
    ): QuizQuestion? {
        val image = TmdbImage.poster(movie.posterPath) ?: return null
        if (image in usedImages) return null
        val year = movie.year ?: return null
        val distractors = (1..10).flatMap { listOf(year - it, year + it) }
            .filter { it != year }
            .distinct()
            .shuffled(random)
            .take(OPTIONS - 1)
            .map { it.toString() }
        if (distractors.size < OPTIONS - 1) return null
        usedImages += image
        return makeQuestion(QuestionType.GUESS_YEAR, image, title = movie.title, correct = year.toString(), distractors)
    }

    private fun buildGuessActor(
        movie: QuizMovie,
        allActors: List<String>,
        usedImages: MutableSet<String>,
    ): QuizQuestion? {
        val image = TmdbImage.poster(movie.posterPath) ?: return null
        if (image in usedImages) return null
        val correct = movie.actors.take(3).randomOrNull(random) ?: return null
        val distractors = allActors.filter { it !in movie.actors }.shuffled(random).take(OPTIONS - 1)
        if (distractors.size < OPTIONS - 1) return null
        usedImages += image
        return makeQuestion(QuestionType.GUESS_ACTOR, image, title = movie.title, correct = correct, distractors)
    }

    private fun makeQuestion(
        type: QuestionType,
        image: String,
        title: String?,
        correct: String,
        distractors: List<String>,
    ): QuizQuestion {
        val options = (distractors + correct).shuffled(random)
        return QuizQuestion(
            type = type,
            imageUrl = image,
            title = title,
            options = options,
            correctIndex = options.indexOf(correct),
        )
    }

    companion object {
        const val MAX_PER_TYPE = 4
        const val OPTIONS = 4
    }
}
