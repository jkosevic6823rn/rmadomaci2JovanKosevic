package rs.edu.raf.rma.showtime.quiz

import rs.edu.raf.rma.showtime.quiz.domain.QuizScoring
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * UBP = BTO * (9 + PVT / 60), ograničeno na 100.00.
 */
class QuizScoringTest {

    @Test
    fun allCorrectWithFullTimeIsCappedAt100() {
        // 10 * (9 + 60/60) = 100
        assertEquals(100.0, QuizScoring.score(correctCount = 10, remainingSeconds = 60))
    }

    @Test
    fun allCorrectWithNoTimeLeft() {
        // 10 * (9 + 0) = 90
        assertEquals(90.0, QuizScoring.score(correctCount = 10, remainingSeconds = 0))
    }

    @Test
    fun zeroCorrectIsZero() {
        assertEquals(0.0, QuizScoring.score(correctCount = 0, remainingSeconds = 45))
    }

    @Test
    fun halfCorrectHalfTime() {
        // 5 * (9 + 30/60) = 5 * 9.5 = 47.5
        assertEquals(47.5, QuizScoring.score(correctCount = 5, remainingSeconds = 30))
    }

    @Test
    fun roundedToTwoDecimals() {
        // 7 * (9 + 13/60) = 7 * 9.21666.. = 64.51666.. -> 64.52
        assertEquals(64.52, QuizScoring.score(correctCount = 7, remainingSeconds = 13))
    }

    @Test
    fun negativeOrOverflowingTimeIsClamped() {
        // remaining se sabija u [0, 60]
        assertEquals(90.0, QuizScoring.score(correctCount = 10, remainingSeconds = -5))
        assertEquals(100.0, QuizScoring.score(correctCount = 10, remainingSeconds = 999))
    }
}
