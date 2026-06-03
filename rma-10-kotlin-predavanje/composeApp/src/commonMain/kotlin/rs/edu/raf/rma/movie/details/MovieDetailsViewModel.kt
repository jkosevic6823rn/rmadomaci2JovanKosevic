package rs.edu.raf.rma.movie.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movie.data.model.Movie
import rs.edu.raf.rma.movie.data.model.MovieImage
import rs.edu.raf.rma.movie.data.model.MovieListItem
import rs.edu.raf.rma.movie.data.model.PersonSummary
import rs.edu.raf.rma.movie.data.repository.MovieRepository
import rs.edu.raf.rma.showtime.library.data.LibraryRepository

data class MovieDetailsState(
    val movie: Movie? = null,
    val cast: List<PersonSummary> = emptyList(),
    val images: List<MovieImage> = emptyList(),
    val trailerKey: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val isFavorite: Boolean = false,
    val inWatchlist: Boolean = false,
    val actionMessage: String? = null,
)

sealed class MovieDetailsIntent {
    object Load : MovieDetailsIntent()
    object Retry : MovieDetailsIntent()
    object ToggleFavorite : MovieDetailsIntent()
    object ToggleWatchlist : MovieDetailsIntent()
    object MessageShown : MovieDetailsIntent()
}

class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val movieId: String = savedStateHandle.get<String>(MOVIE_ID) ?: ""

    private val _state = MutableStateFlow(MovieDetailsState())
    val state: StateFlow<MovieDetailsState> = _state.asStateFlow()

    init {
        processIntent(MovieDetailsIntent.Load)
        observeLibraryState()
    }

    fun processIntent(intent: MovieDetailsIntent) {
        when (intent) {
            is MovieDetailsIntent.Load, is MovieDetailsIntent.Retry -> loadDetails()
            is MovieDetailsIntent.ToggleFavorite -> toggleFavorite()
            is MovieDetailsIntent.ToggleWatchlist -> toggleWatchlist()
            is MovieDetailsIntent.MessageShown -> _state.value = _state.value.copy(actionMessage = null)
        }
    }

    private fun loadDetails() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                val movie = repository.getMovie(movieId)
                val cast = repository.getCast(movieId)
                val images = repository.getImages(movieId)
                val videos = repository.getVideos(movieId)
                _state.value = _state.value.copy(
                    movie = movie,
                    cast = cast,
                    images = images,
                    trailerKey = videos.firstOrNull()?.key,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Greška pri učitavanju"
                )
            }
        }
    }

    private fun observeLibraryState() {
        viewModelScope.launch {
            libraryRepository.observeIsFavorite(movieId).collect { isFav ->
                _state.value = _state.value.copy(isFavorite = isFav)
            }
        }
        viewModelScope.launch {
            libraryRepository.observeInWatchlist(movieId).collect { inList ->
                _state.value = _state.value.copy(inWatchlist = inList)
            }
        }
    }

    private fun toggleFavorite() {
        val snapshot = currentSnapshot() ?: return
        viewModelScope.launch {
            libraryRepository.toggleFavorite(snapshot).onFailure {
                _state.value = _state.value.copy(actionMessage = "Couldn't update Favorites.")
            }
        }
    }

    private fun toggleWatchlist() {
        val snapshot = currentSnapshot() ?: return
        viewModelScope.launch {
            libraryRepository.toggleWatchlist(snapshot).onFailure {
                _state.value = _state.value.copy(actionMessage = "Couldn't update Watchlist.")
            }
        }
    }

    private fun currentSnapshot(): MovieListItem? {
        val movie = _state.value.movie ?: return null
        return MovieListItem(
            imdbId = movie.imdbId,
            title = movie.title,
            year = movie.year,
            imdbRating = movie.imdbRating,
            imdbVotes = movie.imdbVotes,
            posterPath = movie.posterPath,
            genres = movie.genres,
        )
    }
}

const val MOVIE_ID = "movieId"
