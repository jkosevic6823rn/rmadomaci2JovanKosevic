package rs.edu.raf.rma.showtime.auth.di

import de.jensklingenberg.ktorfit.Ktorfit
import io.ktor.client.HttpClient
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.bind
import org.koin.dsl.module
import rs.edu.raf.rma.networking.di.Qualifiers
import rs.edu.raf.rma.showtime.auth.data.network.ShowtimeAuthApi
import rs.edu.raf.rma.showtime.auth.data.network.createShowtimeAuthApi
import rs.edu.raf.rma.showtime.auth.data.repository.ShowtimeAuthRepository
import rs.edu.raf.rma.showtime.auth.domain.AuthRepository
import rs.edu.raf.rma.showtime.auth.login.LoginViewModel
import rs.edu.raf.rma.showtime.auth.register.RegisterViewModel

val showtimeAuthModule = module {

    single<ShowtimeAuthApi> {
        Ktorfit.Builder()
            .httpClient(get<HttpClient>(Qualifiers.Authenticated))
            .baseUrl("https://rma.finlab.rs/")
            .build()
            .createShowtimeAuthApi()
    }

    single<ShowtimeAuthRepository> {
        ShowtimeAuthRepository(api = get(), authStore = get())
    } bind AuthRepository::class

    viewModelOf(::LoginViewModel)
    viewModelOf(::RegisterViewModel)
}
