package rs.edu.raf.rma.showtime.library.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/** Film u korisnikovoj watchlist listi. Vidi [FavoriteEntity] za semantiku. */
@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val posterPath: String?,
    val addedAt: Long,
)
