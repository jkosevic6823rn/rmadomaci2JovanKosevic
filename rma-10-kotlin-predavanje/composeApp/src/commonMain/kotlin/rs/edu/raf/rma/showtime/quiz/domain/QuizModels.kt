package rs.edu.raf.rma.showtime.quiz.domain

enum class QuestionType {
    GUESS_MOVIE,
    GUESS_YEAR,
    GUESS_ACTOR,
}

/**
 * Jedno kviz pitanje, već pripremljeno za prikaz.
 * [title] je null kada naslov ne sme da se prikaže (Guess the Movie).
 */
data class QuizQuestion(
    val type: QuestionType,
    val imageUrl: String,
    val title: String?,
    val options: List<String>,
    val correctIndex: Int,
)

/** Obogaćeni film koji generator koristi kao izvor za pitanja. */
data class QuizMovie(
    val imdbId: String,
    val title: String,
    val year: Int?,
    val posterPath: String?,
    val backdropPath: String?,
    val actors: List<String>,
)
