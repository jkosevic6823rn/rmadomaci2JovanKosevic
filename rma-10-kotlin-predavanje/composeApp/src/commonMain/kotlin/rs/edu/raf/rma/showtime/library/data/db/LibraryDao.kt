package rs.edu.raf.rma.showtime.library.data.db

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface LibraryDao {

    // --- Favorites ---

    @Query("SELECT * FROM favorites ORDER BY addedAt DESC")
    fun observeFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE imdbId = :id)")
    fun observeIsFavorite(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE imdbId = :id)")
    suspend fun isFavorite(id: String): Boolean

    @Query("SELECT COUNT(*) FROM favorites")
    fun observeFavoritesCount(): Flow<Int>

    @Upsert
    suspend fun upsertFavorite(movie: FavoriteEntity)

    @Upsert
    suspend fun upsertFavorites(movies: List<FavoriteEntity>)

    @Query("DELETE FROM favorites WHERE imdbId = :id")
    suspend fun deleteFavorite(id: String)

    @Query("DELETE FROM favorites")
    suspend fun clearFavorites()

    @Transaction
    suspend fun replaceFavorites(movies: List<FavoriteEntity>) {
        clearFavorites()
        upsertFavorites(movies)
    }

    // --- Watchlist ---

    @Query("SELECT * FROM watchlist ORDER BY addedAt DESC")
    fun observeWatchlist(): Flow<List<WatchlistEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbId = :id)")
    fun observeInWatchlist(id: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM watchlist WHERE imdbId = :id)")
    suspend fun isInWatchlist(id: String): Boolean

    @Query("SELECT COUNT(*) FROM watchlist")
    fun observeWatchlistCount(): Flow<Int>

    @Upsert
    suspend fun upsertWatchlistItem(movie: WatchlistEntity)

    @Upsert
    suspend fun upsertWatchlist(movies: List<WatchlistEntity>)

    @Query("DELETE FROM watchlist WHERE imdbId = :id")
    suspend fun deleteWatchlist(id: String)

    @Query("DELETE FROM watchlist")
    suspend fun clearWatchlist()

    @Transaction
    suspend fun replaceWatchlist(movies: List<WatchlistEntity>) {
        clearWatchlist()
        upsertWatchlist(movies)
    }

    // --- Logout / brisanje lokalnih korisničkih podataka ---

    @Transaction
    suspend fun clearUserData() {
        clearFavorites()
        clearWatchlist()
    }
}
