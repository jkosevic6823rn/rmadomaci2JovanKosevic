package rs.edu.raf.rma.showtime.quiz.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface QuizDao {

    // --- Pool filmova ---

    @Upsert
    suspend fun upsertMovies(movies: List<QuizMovieEntity>)

    @Query("SELECT COUNT(*) FROM quiz_movies WHERE posterPath IS NOT NULL")
    suspend fun countMoviesWithPoster(): Int

    @Query("SELECT * FROM quiz_movies WHERE posterPath IS NOT NULL")
    suspend fun moviesWithPoster(): List<QuizMovieEntity>

    @Query("UPDATE quiz_movies SET backdropPath = :backdrop, actors = :actors WHERE imdbId = :id")
    suspend fun updateEnrichment(id: String, backdrop: String?, actors: String?)

    // --- Rezultati / statistika ---

    @Insert
    suspend fun insertResult(result: QuizResultEntity)

    @Query("SELECT MAX(score) FROM quiz_results")
    fun observeBestScore(): Flow<Double?>

    @Query("SELECT COUNT(*) FROM quiz_results")
    fun observeGamesPlayed(): Flow<Int>

    @Query("SELECT * FROM quiz_results ORDER BY playedAt DESC")
    fun observeResults(): Flow<List<QuizResultEntity>>
}
