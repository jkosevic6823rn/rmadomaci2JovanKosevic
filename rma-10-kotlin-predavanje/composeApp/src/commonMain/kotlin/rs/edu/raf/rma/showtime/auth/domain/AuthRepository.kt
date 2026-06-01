package rs.edu.raf.rma.showtime.auth.domain

interface AuthRepository {
    suspend fun login(username: String, password: String): Result<Unit>
    suspend fun signup(fullName: String, username: String, password: String): Result<Unit>
    suspend fun logout()
}
