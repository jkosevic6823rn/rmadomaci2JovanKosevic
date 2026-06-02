package rs.edu.raf.rma.showtime.quiz.domain

import kotlin.math.min
import kotlin.math.round

/**
 * Formula bodovanja iz specifikacije:
 * UBP = BTO * (9 + PVT / MVT), ograničeno na najviše 100.00
 * gde je MVT = 60s, PVT = preostalo vreme, BTO = broj tačnih odgovora.
 */
object QuizScoring {
    const val TOTAL_QUESTIONS = 10
    const val TIME_LIMIT_SECONDS = 60

    fun score(correctCount: Int, remainingSeconds: Int): Double {
        val pvt = remainingSeconds.coerceIn(0, TIME_LIMIT_SECONDS)
        val raw = correctCount * (9.0 + pvt.toDouble() / TIME_LIMIT_SECONDS)
        val capped = min(raw, 100.0)
        return round(capped * 100.0) / 100.0
    }
}
