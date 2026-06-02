package rs.edu.raf.rma.showtime.quiz

import rs.edu.raf.rma.showtime.quiz.domain.QuizQuestion

interface QuizContract {

    sealed interface UiState {
        /** Početni ekran sa dugmetom za start. */
        data class Idle(val bestScore: Double?) : UiState

        /** Priprema pool-a / sastavljanje sesije. */
        data object Loading : UiState

        /** Nema dovoljno filmova u lokalnoj bazi. */
        data class NotEnough(val message: String) : UiState

        data class Error(val message: String) : UiState

        data class Playing(
            val questions: List<QuizQuestion>,
            val index: Int,
            val selectedIndex: Int?,
            val revealed: Boolean,
            val correctCount: Int,
            val remainingSeconds: Int,
        ) : UiState {
            val current: QuizQuestion get() = questions[index]
            val questionNumber: Int get() = index + 1
            val total: Int get() = questions.size
        }

        data class Result(
            val score: Double,
            val correct: Int,
            val incorrect: Int,
            val timeUsedSeconds: Int,
        ) : UiState
    }

    sealed interface UiEvent {
        data object Start : UiEvent
        data class SelectAnswer(val optionIndex: Int) : UiEvent
        data object PlayAgain : UiEvent
    }
}
