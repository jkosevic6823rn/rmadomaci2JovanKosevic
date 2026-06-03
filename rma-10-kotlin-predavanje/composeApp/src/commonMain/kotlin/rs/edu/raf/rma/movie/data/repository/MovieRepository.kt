package rs.edu.raf.rma.movie.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import rs.edu.raf.rma.movie.data.db.MovieDao
import rs.edu.raf.rma.movie.data.db.MovieEntity
import rs.edu.raf.rma.movie.data.model.FilterParams
import rs.edu.raf.rma.movie.data.model.Genre
import rs.edu.raf.rma.movie.data.model.Movie
import rs.edu.raf.rma.movie.data.model.MovieImage
import rs.edu.raf.rma.movie.data.model.MovieListItem
import rs.edu.raf.rma.movie.data.model.PersonSummary
import rs.edu.raf.rma.movie.data.model.Video
import rs.edu.raf.rma.movie.data.network.MovieService

/**
 * Katalog ide preko Room-a (SSOT): UI posmatra [observeMovies], a [refreshMovies]
 * povlači sa servera (sort/filter primenjuje API) i zamenjuje keš. Detalji
 * (cast/slike/trailer) se učitavaju uživo jer nisu deo keširanog kataloga.
 */
class MovieRepository(
    private val service: MovieService,
    private val movieDao: MovieDao,
) {

    fun observeMovies(): Flow<List<MovieListItem>> =
        movieDao.observeMovies().map { list -> list.map { it.toListItem() } }

    suspend fun refreshMovies(
        sortBy: String = "imdb_rating",
        filters: FilterParams = FilterParams(),
    ) {
        val response = service.getMovies(
            pageSize = 30,
            sortBy = sortBy,
            sortOrder = "desc",
            query = filters.query.takeIf { it.isNotEmpty() },
            genreId = filters.genreId,
            minYear = filters.minYear,
            maxYear = filters.maxYear,
            minRating = filters.minRating.takeIf { it > 0f },
        )
        movieDao.replaceMovies(
            response.items.mapIndexed { index, item -> item.toEntity(index) },
        )
    }

    suspend fun getMovie(id: String): Movie = service.getMovie(id)

    suspend fun getCast(id: String): List<PersonSummary> =
        service.getCast(id, pageSize = 10).items

    suspend fun getImages(id: String): List<MovieImage> =
        service.getImages(id, type = "backdrop").backdrops.take(3)

    suspend fun getVideos(id: String): List<Video> =
        service.getVideos(id, type = "Trailer")

    suspend fun getGenres(): List<Genre> = service.getGenres()
}

private const val GENRE_SEPARATOR = "|"

private fun MovieListItem.toEntity(position: Int): MovieEntity = MovieEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genres = genres.joinToString(GENRE_SEPARATOR) { it.name }.ifBlank { null },
    position = position,
)

private fun MovieEntity.toListItem(): MovieListItem = MovieListItem(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    imdbVotes = imdbVotes,
    posterPath = posterPath,
    genres = genres
        ?.split(GENRE_SEPARATOR)
        ?.filter { it.isNotBlank() }
        ?.map { Genre(id = 0, name = it) }
        ?: emptyList(),
)
