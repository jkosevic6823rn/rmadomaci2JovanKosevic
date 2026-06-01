package rs.edu.raf.rma.showtime.auth.domain

sealed class AuthError(message: String, cause: Throwable? = null) : Throwable(message, cause) {
    data object InvalidCredentials : AuthError("Invalid username or password")
    data object UsernameTaken : AuthError("Username already taken")
    data object Validation : AuthError("Invalid input")
    data object Network : AuthError("Network error, check your connection")
    class Unknown(cause: Throwable) : AuthError(cause.message ?: "Unknown error", cause)
}
