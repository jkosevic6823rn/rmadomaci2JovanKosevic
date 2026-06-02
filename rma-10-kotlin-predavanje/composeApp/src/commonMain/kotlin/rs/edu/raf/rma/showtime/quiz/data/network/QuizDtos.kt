package rs.edu.raf.rma.showtime.quiz.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PostQuizRequest(
    val score: Double,
    val category: Int = 1,
)

@Serializable
data class PostQuizResultResponse(
    val result: QuizResultDto,
    val ranking: Int,
)

@Serializable
data class QuizResultDto(
    val id: Long,
    val category: Int,
    val score: Double,
    @SerialName("played_at") val playedAt: Long,
)
