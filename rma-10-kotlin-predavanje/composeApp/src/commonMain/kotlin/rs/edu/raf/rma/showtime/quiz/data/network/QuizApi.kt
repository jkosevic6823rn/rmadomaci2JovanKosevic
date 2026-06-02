package rs.edu.raf.rma.showtime.quiz.data.network

import de.jensklingenberg.ktorfit.http.Body
import de.jensklingenberg.ktorfit.http.GET
import de.jensklingenberg.ktorfit.http.POST
import de.jensklingenberg.ktorfit.http.Query
import rs.edu.raf.rma.movie.data.model.PaginatedResponse

/**
 * Autentikovani Showtime endpoint-i za kviz (leaderboard + istorija).
 * Pitanja se generišu na klijentu; serveru se šalje samo finalni skor.
 */
interface QuizApi {

    @POST("leaderboard")
    suspend fun postQuizResult(@Body body: PostQuizRequest): PostQuizResultResponse

    @GET("me/quiz-results")
    suspend fun getMyResults(
        @Query("page") page: Int = 1,
        @Query("page_size") pageSize: Int = 20,
    ): PaginatedResponse<QuizResultDto>
}
