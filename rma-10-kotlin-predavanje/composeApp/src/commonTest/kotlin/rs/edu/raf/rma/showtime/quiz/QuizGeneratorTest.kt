package rs.edu.raf.rma.showtime.quiz

import rs.edu.raf.rma.showtime.quiz.domain.QuizGenerator
import rs.edu.raf.rma.showtime.quiz.domain.QuizMovie
import rs.edu.raf.rma.showtime.quiz.domain.QuizScoring
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QuizGeneratorTest {

    private fun sampleMovies(n: Int): List<QuizMovie> = (1..n).map { i ->
        QuizMovie(
            imdbId = "tt$i",
            title = "Movie $i",
            year = 1980 + i,
            posterPath = "/poster$i.jpg",
            backdropPath = "/backdrop$i.jpg",
            actors = listOf("Actor ${i}a", "Actor ${i}b", "Actor ${i}c"),
        )
    }

    @Test
    fun producesRequestedNumberOfQuestions() {
        val generator = QuizGenerator(Random(42))
        val questions = generator.generate(sampleMovies(15))
        assertEquals(QuizScoring.TOTAL_QUESTIONS, questions.size)
    }

    @Test
    fun everyQuestionHasFourOptionsWithValidCorrectIndex() {
        val generator = QuizGenerator(Random(7))
        val questions = generator.generate(sampleMovies(15))
        questions.forEach { q ->
            assertEquals(QuizGenerator.OPTIONS, q.options.size)
            assertTrue(q.correctIndex in q.options.indices)
            // tačan odgovor mora postojati među opcijama i opcije su jedinstvene
            assertEquals(q.options.size, q.options.distinct().size)
        }
    }

    @Test
    fun noImageRepeatsWithinSession() {
        val generator = QuizGenerator(Random(123))
        val questions = generator.generate(sampleMovies(15))
        val images = questions.map { it.imageUrl }
        assertEquals(images.size, images.distinct().size)
    }

    @Test
    fun noTypeExceedsMaxPerType() {
        val generator = QuizGenerator(Random(999))
        val questions = generator.generate(sampleMovies(15))
        val perType = questions.groupingBy { it.type }.eachCount()
        perType.forEach { (_, count) ->
            assertTrue(count <= QuizGenerator.MAX_PER_TYPE, "Tip se pojavio $count puta")
        }
    }

    @Test
    fun stableWhenNotEnoughMovies() {
        // Premalo filmova -> generator stabilno vrati manje od 10 pitanja (bez pada).
        val generator = QuizGenerator(Random(1))
        val questions = generator.generate(sampleMovies(3))
        assertTrue(questions.size <= 3)
    }
}
