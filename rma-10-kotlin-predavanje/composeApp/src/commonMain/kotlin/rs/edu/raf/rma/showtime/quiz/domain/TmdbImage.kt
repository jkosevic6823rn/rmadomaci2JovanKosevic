package rs.edu.raf.rma.showtime.quiz.domain

/**
 * Gradi pune URL-ove slika od path-a koji API vraća.
 * Formula: {base}{size}{path}, npr. https://image.tmdb.org/t/p/w342/abc.jpg
 */
object TmdbImage {
    private const val BASE = "https://image.tmdb.org/t/p/"

    fun poster(path: String?, size: String = "w342"): String? =
        path?.let { BASE + size + it }

    fun backdrop(path: String?, size: String = "w780"): String? =
        path?.let { BASE + size + it }
}
