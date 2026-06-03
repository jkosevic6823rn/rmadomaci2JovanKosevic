package rs.edu.raf.rma.showtime.library.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module
import rs.edu.raf.rma.core.db.AppDatabase
import rs.edu.raf.rma.networking.di.Qualifiers
import rs.edu.raf.rma.showtime.library.LibraryViewModel
import rs.edu.raf.rma.showtime.library.data.LibraryRepository
import rs.edu.raf.rma.showtime.library.data.network.ShowtimeUserApi
import rs.edu.raf.rma.showtime.library.data.network.createShowtimeUserApi
import rs.edu.raf.rma.showtime.session.SessionManager

val libraryModule = module {

    single<ShowtimeUserApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl("https://rma.finlab.rs/")
            .build()
            .createShowtimeUserApi()
    }

    single { get<AppDatabase>().libraryDao() }

    single { LibraryRepository(api = get(), dao = get()) }

    single { SessionManager(authStore = get(), libraryDao = get(), quizDao = get()) }

    viewModelOf(::LibraryViewModel)
}
