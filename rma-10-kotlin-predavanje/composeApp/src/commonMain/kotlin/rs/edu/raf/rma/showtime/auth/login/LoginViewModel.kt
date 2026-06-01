package rs.edu.raf.rma.showtime.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import rs.edu.raf.rma.showtime.auth.domain.AuthError
import rs.edu.raf.rma.showtime.auth.domain.AuthRepository

class LoginViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(LoginContract.UiState())
    val state = _state.asStateFlow()

    private val _effects = Channel<LoginContract.SideEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val events = MutableSharedFlow<LoginContract.UiEvent>()
    fun setEvent(event: LoginContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
    }

    private fun setState(reducer: LoginContract.UiState.() -> LoginContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is LoginContract.UiEvent.UsernameChanged ->
                        setState { copy(username = event.value, error = null) }
                    is LoginContract.UiEvent.PasswordChanged ->
                        setState { copy(password = event.value, error = null) }
                    LoginContract.UiEvent.Submit -> submit()
                    LoginContract.UiEvent.DismissError ->
                        setState { copy(error = null) }
                }
            }
        }
    }

    private fun submit() {
        val current = _state.value
        if (!current.canSubmit) return

        viewModelScope.launch {
            setState { copy(isLoading = true, error = null) }
            authRepository.login(
                username = current.username.trim(),
                password = current.password,
            ).onSuccess {
                _effects.send(LoginContract.SideEffect.LoggedIn)
            }.onFailure { throwable ->
                val authError = throwable as? AuthError ?: AuthError.Unknown(throwable)
                setState { copy(error = authError) }
            }
            setState { copy(isLoading = false) }
        }
    }
}
