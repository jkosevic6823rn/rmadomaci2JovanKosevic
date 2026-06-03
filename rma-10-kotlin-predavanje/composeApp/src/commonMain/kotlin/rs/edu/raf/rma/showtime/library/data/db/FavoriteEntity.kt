package rs.edu.raf.rma.showtime.library.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Film u korisnikovoj listi favorita. Snimak osnovnih podataka je denormalizovan
 * da lista radi offline i nezavisno od keša kataloga. [addedAt] daje redosled
 * (most-recent-first). Room = SSOT; UI gleda flow-ove iz [LibraryDao].
 */
@Entity(tableName = "favorites")
data class FavoriteEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val posterPath: String?,
    val addedAt: Long,
)
