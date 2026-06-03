package rs.edu.raf.rma.showtime.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movie.data.model.MovieListItem
import rs.edu.raf.rma.showtime.library.data.LibraryRepository

class LibraryViewModel(
    private val repository: LibraryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LibraryContract.UiState())
    val state = _state.asStateFlow()

    private val _effects = Channel<LibraryContract.SideEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    init {
        observeFavorites()
        observeWatchlist()
        setEvent(LibraryContract.UiEvent.Refresh)
    }

    fun setEvent(event: LibraryContract.UiEvent) {
        when (event) {
            LibraryContract.UiEvent.Refresh -> refresh()
            is LibraryContract.UiEvent.RemoveFavorite -> removeFavorite(event.movie)
            is LibraryContract.UiEvent.RemoveFromWatchlist -> removeFromWatchlist(event.movie)
        }
    }

    private fun setState(reducer: LibraryContract.UiState.() -> LibraryContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private fun observeFavorites() {
        viewModelScope.launch {
            repository.observeFavorites().collect { favorites ->
                setState { copy(favorites = favorites) }
            }
        }
    }

    private fun observeWatchlist() {
        viewModelScope.launch {
            repository.observeWatchlist().collect { watchlist ->
                setState { copy(watchlist = watchlist) }
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            setState { copy(isRefreshing = true) }
            repository.refresh()
            setState { copy(isRefreshing = false) }
        }
    }

    private fun removeFavorite(movie: MovieListItem) {
        viewModelScope.launch {
            repository.toggleFavorite(movie).onFailure {
                _effects.send(LibraryContract.SideEffect.Message("Couldn't remove from Favorites."))
            }
        }
    }

    private fun removeFromWatchlist(movie: MovieListItem) {
        viewModelScope.launch {
            repository.toggleWatchlist(movie).onFailure {
                _effects.send(LibraryContract.SideEffect.Message("Couldn't remove from Watchlist."))
            }
        }
    }
}
