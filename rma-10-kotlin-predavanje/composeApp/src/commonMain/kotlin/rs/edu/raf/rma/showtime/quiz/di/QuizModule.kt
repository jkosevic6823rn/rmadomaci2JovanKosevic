package rs.edu.raf.rma.showtime.quiz.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.networking.di.Qualifiers
import rs.edu.raf.rma.showtime.quiz.QuizViewModel
import rs.edu.raf.rma.showtime.quiz.data.QuizRepository
import rs.edu.raf.rma.showtime.quiz.data.network.QuizApi
import rs.edu.raf.rma.showtime.quiz.data.network.createQuizApi
import rs.edu.raf.rma.showtime.quiz.domain.QuizGenerator

val quizModule = module {

    single<QuizApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl("https://rma.finlab.rs/")
            .build()
            .createQuizApi()
    }

    single { QuizGenerator() }

    single { get<AppDatabase>().quizDao() }

    single {
        QuizRepository(
            movieService = get(),
            quizApi = get(),
            dao = get(),
            generator = get(),
        )
    }

    viewModelOf(::QuizViewModel)
}
