package rs.edu.raf.rma.showtime.library.data.network

import de.jensklingenberg.ktorfit.http.DELETE
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Path
import rs.edu.raf.rma.movie.data.model.MovieListItem

/**
 * Autentikovani Showtime endpoint-i za korisnikove liste (favorites + watchlist).
 * Liste nisu paginirane i vraćaju se most-recent-first. POST/DELETE su idempotentni.
 */
interface ShowtimeUserApi {

    @GET("me/favorites")
    suspend fun getFavorites(): List<MovieListItem>

    @POST("me/favorites/{movie_id}")
    suspend fun addFavorite(@Path("movie_id") movieId: String)

    @DELETE("me/favorites/{movie_id}")
    suspend fun removeFavorite(@Path("movie_id") movieId: String)

    @GET("me/watchlist")
    suspend fun getWatchlist(): List<MovieListItem>

    @POST("me/watchlist/{movie_id}")
    suspend fun addWatchlist(@Path("movie_id") movieId: String)

    @DELETE("me/watchlist/{movie_id}")
    suspend fun removeWatchlist(@Path("movie_id") movieId: String)
}
