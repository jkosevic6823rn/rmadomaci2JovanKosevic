package rs.edu.raf.rma.showtime.auth.data.network

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST

interface ShowtimeAuthApi {

    @POST("auth/signup")
    suspend fun signup(@Body body: SignupRequest): AuthResponseDto

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponseDto

    @GET("me")
    suspend fun me(): UserDto
}
