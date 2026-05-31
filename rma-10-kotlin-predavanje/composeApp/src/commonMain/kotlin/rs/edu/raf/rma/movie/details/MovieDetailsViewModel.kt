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
import rs.edu.raf.rma.movie.data.model.PersonSummary
import rs.edu.raf.rma.movie.data.repository.MovieRepository

data class MovieDetailsState(
    val movie: Movie? = null,
    val cast: List<PersonSummary> = emptyList(),
    val images: List<MovieImage> = emptyList(),
    val trailerKey: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class MovieDetailsIntent {
    object Load : MovieDetailsIntent()
    object Retry : MovieDetailsIntent()
}

class MovieDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: MovieRepository
) : ViewModel() {

    private val movieId: String = savedStateHandle.get<String>(MOVIE_ID) ?: ""

    private val _state = MutableStateFlow(MovieDetailsState())
    val state: StateFlow<MovieDetailsState> = _state.asStateFlow()

    init {
        processIntent(MovieDetailsIntent.Load)
    }

    fun processIntent(intent: MovieDetailsIntent) {
        when (intent) {
            is MovieDetailsIntent.Load, is MovieDetailsIntent.Retry -> loadDetails()
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
}

const val MOVIE_ID = "movieId"
