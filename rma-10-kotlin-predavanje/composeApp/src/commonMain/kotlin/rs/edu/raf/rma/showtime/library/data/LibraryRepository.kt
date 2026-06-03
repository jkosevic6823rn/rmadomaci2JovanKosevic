package rs.edu.raf.rma.showtime.library.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlin.time.Clock
import rs.edu.raf.rma.movie.data.model.MovieListItem
import rs.edu.raf.rma.showtime.library.data.db.FavoriteEntity
import rs.edu.raf.rma.showtime.library.data.db.LibraryDao
import rs.edu.raf.rma.showtime.library.data.db.WatchlistEntity
import rs.edu.raf.rma.showtime.library.data.network.ShowtimeUserApi

/**
 * Dve nezavisne liste (Favorites + Watchlist) sa Room-om kao SSOT.
 * UI uvek čita Room flow-ove; mreža samo sinhronizuje. Toggle je optimistički:
 * Room se menja odmah, pa ako server vrati grešku, vraćamo prethodno stanje.
 */
class LibraryRepository(
    private val api: ShowtimeUserApi,
    private val dao: LibraryDao,
) {

    // --- Observacija (Room = SSOT) ---

    fun observeFavorites(): Flow<List<MovieListItem>> =
        dao.observeFavorites().map { list -> list.map { it.toListItem() } }

    fun observeWatchlist(): Flow<List<MovieListItem>> =
        dao.observeWatchlist().map { list -> list.map { it.toListItem() } }

    fun observeIsFavorite(id: String): Flow<Boolean> = dao.observeIsFavorite(id)
    fun observeInWatchlist(id: String): Flow<Boolean> = dao.observeInWatchlist(id)

    fun observeFavoritesCount(): Flow<Int> = dao.observeFavoritesCount()
    fun observeWatchlistCount(): Flow<Int> = dao.observeWatchlistCount()

    // --- Sinhronizacija sa serverom ---

    /** Povuče obe liste sa servera i upiše ih u Room. Greška po listi se ignoriše (ostaje keš). */
    suspend fun refresh() {
        runCatching { dao.replaceFavorites(api.getFavorites().map { it.toFavoriteEntity() }) }
        runCatching { dao.replaceWatchlist(api.getWatchlist().map { it.toWatchlistEntity() }) }
    }

    // --- Optimistički toggle ---

    suspend fun toggleFavorite(movie: MovieListItem): Result<Unit> {
        val wasFavorite = dao.isFavorite(movie.imdbId)
        return if (wasFavorite) {
            dao.deleteFavorite(movie.imdbId)
            runApiCatching(
                api = { api.removeFavorite(movie.imdbId) },
                rollback = { dao.upsertFavorite(movie.toFavoriteEntity()) },
            )
        } else {
            dao.upsertFavorite(movie.toFavoriteEntity())
            runApiCatching(
                api = { api.addFavorite(movie.imdbId) },
                rollback = { dao.deleteFavorite(movie.imdbId) },
            )
        }
    }

    suspend fun toggleWatchlist(movie: MovieListItem): Result<Unit> {
        val wasInWatchlist = dao.isInWatchlist(movie.imdbId)
        return if (wasInWatchlist) {
            dao.deleteWatchlist(movie.imdbId)
            runApiCatching(
                api = { api.removeWatchlist(movie.imdbId) },
                rollback = { dao.upsertWatchlistItem(movie.toWatchlistEntity()) },
            )
        } else {
            dao.upsertWatchlistItem(movie.toWatchlistEntity())
            runApiCatching(
                api = { api.addWatchlist(movie.imdbId) },
                rollback = { dao.deleteWatchlist(movie.imdbId) },
            )
        }
    }

    private suspend fun runApiCatching(
        api: suspend () -> Unit,
        rollback: suspend () -> Unit,
    ): Result<Unit> = try {
        api()
        Result.success(Unit)
    } catch (cancelled: CancellationException) {
        throw cancelled
    } catch (t: Throwable) {
        rollback()
        Result.failure(t)
    }
}

private fun MovieListItem.toFavoriteEntity(): FavoriteEntity = FavoriteEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    posterPath = posterPath,
    addedAt = Clock.System.now().toEpochMilliseconds(),
)

private fun MovieListItem.toWatchlistEntity(): WatchlistEntity = WatchlistEntity(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    posterPath = posterPath,
    addedAt = Clock.System.now().toEpochMilliseconds(),
)

private fun FavoriteEntity.toListItem(): MovieListItem = MovieListItem(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    posterPath = posterPath,
)

private fun WatchlistEntity.toListItem(): MovieListItem = MovieListItem(
    imdbId = imdbId,
    title = title,
    year = year,
    imdbRating = imdbRating,
    posterPath = posterPath,
)
