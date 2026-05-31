package rs.edu.raf.rma.movie.data.network

import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.Path
import de.jensklingenberg.ktorfit.http.Query
import rs.edu.raf.rma.movie.data.model.Genre
import rs.edu.raf.rma.movie.data.model.Movie
import rs.edu.raf.rma.movie.data.model.MovieImages
import rs.edu.raf.rma.movie.data.model.MovieListItem
import rs.edu.raf.rma.movie.data.model.PaginatedResponse
import rs.edu.raf.rma.movie.data.model.PersonSummary
import rs.edu.raf.rma.movie.data.model.Video

interface MovieService {

    @GET("movies")
    suspend fun getMovies(
        @Query("page_size") pageSize: Int = 30,
        @Query("sort_by") sortBy: String = "imdb_rating",
        @Query("sort_order") sortOrder: String = "desc",
        @Query("query") query: String? = null,
        @Query("genre_id") genreId: Int? = null,
        @Query("min_year") minYear: Int? = null,
        @Query("max_year") maxYear: Int? = null,
        @Query("min_rating") minRating: Float? = null
    ): PaginatedResponse<MovieListItem>

    @GET("movies/{id}")
    suspend fun getMovie(@Path("id") id: String): Movie

    @GET("movies/{id}/cast")
    suspend fun getCast(
        @Path("id") id: String,
        @Query("page_size") pageSize: Int = 10
    ): PaginatedResponse<PersonSummary>

    @GET("movies/{id}/images")
    suspend fun getImages(
        @Path("id") id: String,
        @Query("type") type: String = "backdrop"
    ): MovieImages

    @GET("movies/{id}/videos")
    suspend fun getVideos(
        @Path("id") id: String,
        @Query("type") type: String = "Trailer"
    ): List<Video>

    @GET("genres")
    suspend fun getGenres(): List<Genre>
}
