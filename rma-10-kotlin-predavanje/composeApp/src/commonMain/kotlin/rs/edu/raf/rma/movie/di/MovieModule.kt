    package rs.edu.raf.rma.movie.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.movie.data.network.MovieService
import rs.edu.raf.rma.movie.data.network.createMovieService
import rs.edu.raf.rma.movie.data.repository.MovieRepository
import rs.edu.raf.rma.movie.details.MovieDetailsViewModel
import rs.edu.raf.rma.movie.filter.FilterViewModel
import rs.edu.raf.rma.movie.list.MovieListViewModel

val movieModule = module {

    single {
        HttpClient {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                })
            }
        }
    }

    single<MovieService> {
        Ktorfit.Builder()
            .baseUrl("https://rma.finlab.rs/")
            .httpClient(get<HttpClient>())
            .build()
            .createMovieService()
    }

    single { MovieRepository(get()) }

    viewModelOf(::MovieListViewModel)
    viewModelOf(::FilterViewModel)
    viewModelOf(::MovieDetailsViewModel)
}
