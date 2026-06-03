package rs.edu.raf.rma.movie.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Keš kataloga filmova (Room = SSOT za katalog). Posle svakog osvežavanja sa
 * servera tabela se zamenjuje rezultatom trenutnog upita; [position] čuva
 * redosled koji je server vratio (sort/filter primenjuje API).
 */
@Entity(tableName = "movies")
data class MovieEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val imdbRating: Float?,
    val imdbVotes: Int?,
    val posterPath: String?,
    /** Imena žanrova razdvojena '|' (za prikaz čipova u listi). */
    val genres: String?,
    val position: Int,
)
