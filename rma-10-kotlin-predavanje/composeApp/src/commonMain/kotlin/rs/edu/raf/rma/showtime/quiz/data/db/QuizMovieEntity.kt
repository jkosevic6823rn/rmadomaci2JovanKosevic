package rs.edu.raf.rma.showtime.quiz.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lokalni pool filmova iz kog se generišu kviz pitanja (Room = SSOT za kviz).
 * Osnovni podaci dolaze bootstrap-om (top filmovi), a [backdropPath] i [actors]
 * se kešraju lenjo prilikom pripreme sesije da bi naredne sesije bile brže.
 */
@Entity(tableName = "quiz_movies")
data class QuizMovieEntity(
    @PrimaryKey val imdbId: String,
    val title: String,
    val year: Int?,
    val posterPath: String?,
    val backdropPath: String? = null,
    /** Imena glumaca razdvojena '|' (keširano nakon enrichment-a). */
    val actors: String? = null,
)
