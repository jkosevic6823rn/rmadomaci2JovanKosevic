package rs.edu.raf.rma.showtime.auth.register

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

class RegisterViewModel(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _state = MutableStateFlow(RegisterContract.UiState())
    val state = _state.asStateFlow()

    private val _effects = Channel<RegisterContract.SideEffect>(Channel.BUFFERED)
    val effects = _effects.receiveAsFlow()

    private val events = MutableSharedFlow<RegisterContract.UiEvent>()
    fun setEvent(event: RegisterContract.UiEvent) {
        viewModelScope.launch { events.emit(event) }
    }

    init {
        observeEvents()
    }

    private fun setState(reducer: RegisterContract.UiState.() -> RegisterContract.UiState) {
        _state.getAndUpdate(reducer)
    }

    private fun observeEvents() {
        viewModelScope.launch {
            events.collect { event ->
                when (event) {
                    is RegisterContract.UiEvent.FullNameChanged ->
                        setState { copy(fullName = event.value, error = null) }
                    is RegisterContract.UiEvent.UsernameChanged ->
                        setState { copy(username = event.value, error = null) }
                    is RegisterContract.UiEvent.PasswordChanged ->
                        setState { copy(password = event.value, error = null) }
                    RegisterContract.UiEvent.Submit -> submit()
                    RegisterContract.UiEvent.DismissError ->
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
            authRepository.signup(
                fullName = current.fullName.trim(),
                username = current.username.trim(),
                password = current.password,
            ).onSuccess {
                _effects.send(RegisterContract.SideEffect.Registered)
            }.onFailure { throwable ->
                val authError = throwable as? AuthError ?: AuthError.Unknown(throwable)
                setState { copy(error = authError) }
            }
            setState { copy(isLoading = false) }
        }
    }
}
