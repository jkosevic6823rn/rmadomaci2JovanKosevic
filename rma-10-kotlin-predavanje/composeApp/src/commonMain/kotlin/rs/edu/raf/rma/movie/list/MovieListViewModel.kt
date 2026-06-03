package rs.edu.raf.rma.movie.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movie.data.model.FilterParams
import rs.edu.raf.rma.movie.data.model.MovieListItem
import rs.edu.raf.rma.movie.data.repository.MovieRepository

data class MovieListState(
    val movies: List<MovieListItem> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val sortBy: String = "imdb_rating",
    val filters: FilterParams = FilterParams()
)

sealed class MovieListIntent {
    object LoadMovies : MovieListIntent()
    data class ChangeSortBy(val sortBy: String) : MovieListIntent()
    data class ApplyFilters(val filters: FilterParams) : MovieListIntent()
}

class MovieListViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _state = MutableStateFlow(MovieListState())
    val state: StateFlow<MovieListState> = _state.asStateFlow()

    init {
        observeMovies()
        processIntent(MovieListIntent.LoadMovies)
    }

    fun processIntent(intent: MovieListIntent) {
        when (intent) {
            is MovieListIntent.LoadMovies -> refresh()
            is MovieListIntent.ChangeSortBy -> {
                _state.value = _state.value.copy(sortBy = intent.sortBy)
                refresh()
            }
            is MovieListIntent.ApplyFilters -> {
                _state.value = _state.value.copy(filters = intent.filters)
                refresh()
            }
        }
    }

    /** Room je SSOT — lista se uvek čita iz baze. */
    private fun observeMovies() {
        viewModelScope.launch {
            repository.observeMovies().collect { movies ->
                _state.value = _state.value.copy(movies = movies)
            }
        }
    }

    /** Povuče sa servera prema trenutnom sort/filteru i upiše u Room. */
    private fun refresh() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, error = null)
            try {
                repository.refreshMovies(
                    sortBy = _state.value.sortBy,
                    filters = _state.value.filters,
                )
                _state.value = _state.value.copy(isLoading = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    error = e.message ?: "Greška pri učitavanju"
                )
            }
        }
    }
}
