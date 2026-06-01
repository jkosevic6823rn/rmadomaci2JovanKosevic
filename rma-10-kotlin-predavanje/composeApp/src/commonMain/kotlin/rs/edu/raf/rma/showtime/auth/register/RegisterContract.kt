package rs.edu.raf.rma.showtime.auth.register

import rs.edu.raf.rma.showtime.auth.domain.AuthError

object RegisterValidation {
    private val USERNAME_REGEX = Regex("^[A-Za-z0-9_]+$")
    const val MIN_USERNAME_LENGTH = 3
    const val MIN_PASSWORD_LENGTH = 8

    fun usernameError(username: String): String? = when {
        username.isBlank() -> "Username is required"
        username.length < MIN_USERNAME_LENGTH -> "At least $MIN_USERNAME_LENGTH characters"
        !USERNAME_REGEX.matches(username) -> "Only letters, digits and underscore"
        else -> null
    }

    fun passwordError(password: String): String? = when {
        password.isBlank() -> "Password is required"
        password.length < MIN_PASSWORD_LENGTH -> "At least $MIN_PASSWORD_LENGTH characters"
        else -> null
    }

    fun fullNameError(fullName: String): String? = when {
        fullName.isBlank() -> "Full name is required"
        else -> null
    }
}

interface RegisterContract {
    data class UiState(
        val fullName: String = "",
        val username: String = "",
        val password: String = "",
        val isLoading: Boolean = false,
        val error: AuthError? = null,
    ) {
        val fullNameError: String? get() =
            if (fullName.isEmpty()) null else RegisterValidation.fullNameError(fullName)
        val usernameError: String? get() =
            if (username.isEmpty()) null else RegisterValidation.usernameError(username)
        val passwordError: String? get() =
            if (password.isEmpty()) null else RegisterValidation.passwordError(password)

        val canSubmit: Boolean
            get() = !isLoading &&
                RegisterValidation.fullNameError(fullName) == null &&
                RegisterValidation.usernameError(username) == null &&
                RegisterValidation.passwordError(password) == null
    }

    sealed class UiEvent {
        data class FullNameChanged(val value: String) : UiEvent()
        data class UsernameChanged(val value: String) : UiEvent()
        data class PasswordChanged(val value: String) : UiEvent()
        data object Submit : UiEvent()
        data object DismissError : UiEvent()
    }

    sealed class SideEffect {
        data object Registered : SideEffect()
    }
}
