package rs.edu.raf.rma.showtime.auth.data.repository

import io.ktor.client.plugins.ClientRequestException
import io.ktor.client.plugins.ResponseException
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.CancellationException
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthData
import rs.edu.raf.rma.showtime.auth.data.network.LoginRequest
import rs.edu.raf.rma.showtime.auth.data.network.ShowtimeAuthApi
import rs.edu.raf.rma.showtime.auth.data.network.SignupRequest
import rs.edu.raf.rma.showtime.auth.domain.AuthError
import rs.edu.raf.rma.showtime.auth.domain.AuthRepository

class ShowtimeAuthRepository(
    private val api: ShowtimeAuthApi,
    private val authStore: AuthStore,
) : AuthRepository {

    override suspend fun login(username: String, password: String): Result<Unit> =
        runAuthCatching(
            block = {
                val response = api.login(LoginRequest(username = username, password = password))
                authStore.setAuthData(AuthData(accessToken = response.accessToken, refreshToken = null))
            },
            mapStatus = { status ->
                when (status) {
                    HttpStatusCode.Unauthorized -> AuthError.InvalidCredentials
                    HttpStatusCode.BadRequest -> AuthError.Validation
                    else -> null
                }
            },
        )

    override suspend fun signup(
        fullName: String,
        username: String,
        password: String,
    ): Result<Unit> = runAuthCatching(
        block = {
            val response = api.signup(
                SignupRequest(fullName = fullName, username = username, password = password),
            )
            authStore.setAuthData(AuthData(accessToken = response.accessToken, refreshToken = null))
        },
        mapStatus = { status ->
            when (status) {
                HttpStatusCode.Conflict -> AuthError.UsernameTaken
                HttpStatusCode.BadRequest -> AuthError.Validation
                else -> null
            }
        },
    )

    override suspend fun logout() {
        authStore.clearAuthData()
    }
}

private suspend inline fun runAuthCatching(
    block: suspend () -> Unit,
    mapStatus: (HttpStatusCode) -> AuthError?,
): Result<Unit> = try {
    block()
    Result.success(Unit)
} catch (cancelled: CancellationException) {
    throw cancelled
} catch (clientError: ClientRequestException) {
    val mapped = mapStatus(clientError.response.status) ?: AuthError.Unknown(clientError)
    Result.failure(mapped)
} catch (responseError: ResponseException) {
    Result.failure(AuthError.Unknown(responseError))
} catch (t: Throwable) {
    Result.failure(AuthError.Network)
}
