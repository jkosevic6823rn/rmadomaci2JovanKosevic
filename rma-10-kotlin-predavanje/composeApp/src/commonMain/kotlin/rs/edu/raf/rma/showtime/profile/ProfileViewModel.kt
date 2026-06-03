package rs.edu.raf.rma.showtime.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.auth.data.network.ShowtimeAuthApi
import rs.edu.raf.rma.showtime.library.data.LibraryRepository
import rs.edu.raf.rma.showtime.quiz.data.QuizRepository
import rs.edu.raf.rma.showtime.session.SessionManager

class ProfileViewModel(
    private val authApi: ShowtimeAuthApi,
    private val sessionManager: SessionManager,
    private val quizRepository: QuizRepository,
    private val libraryRepository: LibraryRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.UiState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
        observeStats()
        syncLibrary()
    }

    fun setEvent(event: ProfileContract.UiEvent) {
        when (event) {
            ProfileContract.UiEvent.Retry -> loadProfile()
            ProfileContract.UiEvent.Logout -> logout()
        }
    }

    private fun setState(reducer: ProfileContract.UiState.() -> ProfileContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private fun loadProfile() {
        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            runCatching { authApi.me() }
                .onSuccess { user ->
                    setState { copy(isLoading = false, fullName = user.fullName, username = user.username) }
                }
                .onFailure {
                    setState { copy(isLoading = false, error = "Could not load profile.") }
                }
        }
    }

    private fun observeStats() {
        viewModelScope.launch {
            quizRepository.observeBestScore().collect { best ->
                setState { copy(bestScore = best) }
            }
        }
        viewModelScope.launch {
            quizRepository.observeGamesPlayed().collect { count ->
                setState { copy(gamesPlayed = count) }
            }
        }
        viewModelScope.launch {
            libraryRepository.observeFavoritesCount().collect { count ->
                setState { copy(favoritesCount = count) }
            }
        }
        viewModelScope.launch {
            libraryRepository.observeWatchlistCount().collect { count ->
                setState { copy(watchlistCount = count) }
            }
        }
    }

    /** Osveži liste sa servera da brojevi budu tačni i kad se Profile otvori prvi. */
    private fun syncLibrary() {
        viewModelScope.launch { libraryRepository.refresh() }
    }

    private fun logout() {
        viewModelScope.launch { sessionManager.logout() }
    }
}
