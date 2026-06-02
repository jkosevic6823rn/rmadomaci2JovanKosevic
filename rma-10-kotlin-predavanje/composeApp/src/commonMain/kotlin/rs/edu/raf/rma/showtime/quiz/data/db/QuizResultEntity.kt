package rs.edu.raf.rma.showtime.quiz.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Lokalna istorija kviz sesija. Iz nje se izvode statistike za Profile ekran
 * (najbolji skor i broj odigranih kvizova).
 */
@Entity(tableName = "quiz_results")
data class QuizResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val score: Double,
    val correctCount: Int,
    /** Epoch milisekunde kada je sesija završena. */
    val playedAt: Long,
    val category: Int = 1,
)
