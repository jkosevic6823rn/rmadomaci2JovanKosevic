package rs.edu.raf.rma.movie.filter

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import rs.edu.raf.rma.movie.data.model.FilterParams
import rs.edu.raf.rma.movie.data.model.Genre
import rs.edu.raf.rma.movie.data.repository.MovieRepository

data class FilterState(
    val query: String = "",
    val selectedGenreId: Int? = null,
    val genres: List<Genre> = emptyList(),
    val minYear: String = "",
    val maxYear: String = "",
    val minRating: Float = 0f,
    val isLoadingGenres: Boolean = false
)

sealed class FilterIntent {
    data class SetQuery(val query: String) : FilterIntent()
    data class SelectGenre(val genreId: Int?) : FilterIntent()
    data class SetMinYear(val year: String) : FilterIntent()
    data class SetMaxYear(val year: String) : FilterIntent()
    data class SetMinRating(val rating: Float) : FilterIntent()
    object ClearAll : FilterIntent()
    object LoadGenres : FilterIntent()
}

class FilterViewModel(private val repository: MovieRepository) : ViewModel() {

    private val _state = MutableStateFlow(FilterState())
    val state: StateFlow<FilterState> = _state.asStateFlow()

    init {
        processIntent(FilterIntent.LoadGenres)
    }

    fun processIntent(intent: FilterIntent) {
        when (intent) {
            is FilterIntent.SetQuery ->
                _state.value = _state.value.copy(query = intent.query)
            is FilterIntent.SelectGenre ->
                _state.value = _state.value.copy(selectedGenreId = intent.genreId)
            is FilterIntent.SetMinYear ->
                _state.value = _state.value.copy(minYear = intent.year)
            is FilterIntent.SetMaxYear ->
                _state.value = _state.value.copy(maxYear = intent.year)
            is FilterIntent.SetMinRating ->
                _state.value = _state.value.copy(minRating = intent.rating)
            is FilterIntent.ClearAll ->
                _state.value = _state.value.copy(
                    query = "", selectedGenreId = null,
                    minYear = "", maxYear = "", minRating = 0f
                )
            is FilterIntent.LoadGenres -> loadGenres()
        }
    }

    fun initializeFromFilters(filters: FilterParams) {
        _state.value = _state.value.copy(
            query = filters.query,
            selectedGenreId = filters.genreId,
            minYear = filters.minYear?.toString() ?: "",
            maxYear = filters.maxYear?.toString() ?: "",
            minRating = filters.minRating
        )
    }

    fun toFilterParams(): FilterParams = FilterParams(
        query = _state.value.query,
        genreId = _state.value.selectedGenreId,
        minYear = _state.value.minYear.toIntOrNull(),
        maxYear = _state.value.maxYear.toIntOrNull(),
        minRating = _state.value.minRating
    )

    private fun loadGenres() {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoadingGenres = true)
            try {
                val genres = repository.getGenres()
                _state.value = _state.value.copy(genres = genres, isLoadingGenres = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoadingGenres = false)
            }
        }
    }
}
