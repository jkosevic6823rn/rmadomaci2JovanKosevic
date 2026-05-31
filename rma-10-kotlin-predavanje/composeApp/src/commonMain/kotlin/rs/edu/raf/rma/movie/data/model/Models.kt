package rs.edu.raf.rma.movie.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Genre(
    val id: Int,
    val name: String
)

@Serializable
data class MovieListItem(
    val imdbId: String,
    val title: String,
    val year: Int? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val posterPath: String? = null,
    val genres: List<Genre> = emptyList()
)

@Serializable
data class Movie(
    val imdbId: String,
    val tmdbId: Int? = null,
    val title: String,
    val originalTitle: String? = null,
    val overview: String? = null,
    val tagline: String? = null,
    val releaseDate: String? = null,
    val year: Int? = null,
    val runtime: Int? = null,
    val budget: Long? = null,
    val revenue: Long? = null,
    val languageCode: String? = null,
    val popularity: Float? = null,
    val imdbRating: Float? = null,
    val imdbVotes: Int? = null,
    val tmdbRating: Float? = null,
    val tmdbVotes: Int? = null,
    val posterPath: String? = null,
    val backdropPath: String? = null,
    val homepage: String? = null,
    val genres: List<Genre> = emptyList()
)

@Serializable
data class PaginatedResponse<T>(
    val page: Int,
    val pageSize: Int,
    val totalItems: Int,
    val totalPages: Int,
    val items: List<T>
)

@Serializable
data class PersonSummary(
    val imdbId: String,
    val name: String,
    val professions: String? = null,
    val department: String? = null,
    val profilePath: String? = null
)

@Serializable
data class MovieImage(
    val filePath: String,
    val width: Int? = null,
    val height: Int? = null,
    val voteAverage: Float? = null,
    val language: String? = null
)

@Serializable
data class MovieImages(
    val posters: List<MovieImage> = emptyList(),
    val backdrops: List<MovieImage> = emptyList(),
    val logos: List<MovieImage> = emptyList()
)

@Serializable
data class Video(
    val key: String,
    val site: String,
    val name: String? = null,
    val type: String? = null,
    val official: Boolean = false,
    val publishedAt: String? = null
)

@Serializable
data class FilterParams(
    val query: String = "",
    val genreId: Int? = null,
    val minYear: Int? = null,
    val maxYear: Int? = null,
    val minRating: Float = 0f
) {
    val activeCount: Int
        get() {
            var count = 0
            if (query.isNotEmpty()) count++
            if (genreId != null) count++
            if (minYear != null || maxYear != null) count++
            if (minRating > 0f) count++
            return count
        }
}
