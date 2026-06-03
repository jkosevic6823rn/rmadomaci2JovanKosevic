package rs.edu.raf.rma.movie.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface MovieDao {

    @Query("SELECT * FROM movies ORDER BY position ASC")
    fun observeMovies(): Flow<List<MovieEntity>>

    @Upsert
    suspend fun upsertMovies(movies: List<MovieEntity>)

    @Query("DELETE FROM movies")
    suspend fun clearMovies()

    /** Atomično zameni keš kataloga rezultatom trenutnog upita. */
    @Transaction
    suspend fun replaceMovies(movies: List<MovieEntity>) {
        clearMovies()
        upsertMovies(movies)
    }
}
