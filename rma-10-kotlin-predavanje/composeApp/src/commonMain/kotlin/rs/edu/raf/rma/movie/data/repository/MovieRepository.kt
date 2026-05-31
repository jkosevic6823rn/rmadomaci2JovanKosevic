package rs.edu.raf.rma.movie.data.repository

import rs.edu.raf.rma.movie.data.model.FilterParams
import rs.edu.raf.rma.movie.data.model.Genre
import rs.edu.raf.rma.movie.data.model.Movie
import rs.edu.raf.rma.movie.data.model.MovieImage
import rs.edu.raf.rma.movie.data.model.MovieListItem
import rs.edu.raf.rma.movie.data.model.PaginatedResponse
import rs.edu.raf.rma.movie.data.model.PersonSummary
import rs.edu.raf.rma.movie.data.model.Video
import rs.edu.raf.rma.movie.data.network.MovieService

class MovieRepository(private val service: MovieService) {

    suspend fun getMovies(
        sortBy: String = "imdb_rating",
        filters: FilterParams = FilterParams()
    ): PaginatedResponse<MovieListItem> = service.getMovies(
        pageSize = 30,
        sortBy = sortBy,
        sortOrder = "desc",
        query = filters.query.takeIf { it.isNotEmpty() },
        genreId = filters.genreId,
        minYear = filters.minYear,
        maxYear = filters.maxYear,
        minRating = filters.minRating.takeIf { it > 0f }
    )

    suspend fun getMovie(id: String): Movie = service.getMovie(id)

    suspend fun getCast(id: String): List<PersonSummary> =
        service.getCast(id, pageSize = 10).items

    suspend fun getImages(id: String): List<MovieImage> =
        service.getImages(id, type = "backdrop").backdrops.take(3)

    suspend fun getVideos(id: String): List<Video> =
        service.getVideos(id, type = "Trailer")

    suspend fun getGenres(): List<Genre> = service.getGenres()
}
