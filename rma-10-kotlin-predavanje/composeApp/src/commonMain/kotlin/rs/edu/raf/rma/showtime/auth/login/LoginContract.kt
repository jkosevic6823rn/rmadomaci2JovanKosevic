package rs.edu.raf.rma.showtime.auth.login

import rs.edu.raf.rma.showtime.auth.domain.AuthError

interface LoginContract {
    data class UiState(
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: AuthError? = null,
    ) {
        val canSubmit: Boolean
            get() = username.isNotBlank() && password.isNotBlank() && !isLoading
    }

    sealed class UiEvent {
        data class UsernameChanged(val value: String) : UiEvent()
        data class PasswordChanged(val value: String) : UiEvent()
        data object Submit : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object LoggedIn : SideEffect()
    }
}
