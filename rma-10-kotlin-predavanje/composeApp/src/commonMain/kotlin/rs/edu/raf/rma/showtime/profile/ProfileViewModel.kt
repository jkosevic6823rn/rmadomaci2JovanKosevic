package rs.edu.raf.rma.showtime.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.auth.data.network.ShowtimeAuthApi
import rs.edu.raf.rma.showtime.auth.domain.AuthRepository
import rs.edu.raf.rma.showtime.quiz.data.QuizRepository

class ProfileViewModel(
    private val authApi: ShowtimeAuthApi,
    private val authRepository: AuthRepository,
    private val quizRepository: QuizRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(ProfileContract.UiState())
    val state = _state.asStateFlow()

    init {
        loadProfile()
        observeStats()
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
    }

    private fun logout() {
        viewModelScope.launch { authRepository.logout() }
    }
}
