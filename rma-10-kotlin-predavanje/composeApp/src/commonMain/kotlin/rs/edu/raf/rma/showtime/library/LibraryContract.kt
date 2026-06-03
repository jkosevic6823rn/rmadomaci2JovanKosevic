package rs.edu.raf.rma.showtime.library

import rs.edu.raf.rma.movie.data.model.MovieListItem

interface LibraryContract {

    data class UiState(
        val favorites: List<MovieListItem> = emptyList(),
        val watchlist: List<MovieListItem> = emptyList(),
        val isRefreshing: Boolean = false,
    )

    sealed interface UiEvent {
        data object Refresh : UiEvent
        data class RemoveFavorite(val movie: MovieListItem) : UiEvent
        data class RemoveFromWatchlist(val movie: MovieListItem) : UiEvent
    }

    sealed interface SideEffect {
        data class Message(val text: String) : SideEffect
    }
}
