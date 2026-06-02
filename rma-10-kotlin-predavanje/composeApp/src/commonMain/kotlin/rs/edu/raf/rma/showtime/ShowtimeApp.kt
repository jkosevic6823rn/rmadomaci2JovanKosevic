package rs.edu.raf.rma.showtime

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import org.koin.compose.koinInject
import rs.edu.raf.rma.core.auth.AuthStore
import rs.edu.raf.rma.core.auth.model.AuthState
import rs.edu.raf.rma.showtime.auth.AuthNavigation
import rs.edu.raf.rma.showtime.home.ShowtimeHome

@Composable
fun ShowtimeApp() {
    val authStore: AuthStore = koinInject()
    val authState by authStore.authState.collectAsState()

    MaterialTheme {
        when (authState) {
            AuthState.Unauthenticated -> AuthNavigation()
            is AuthState.Authenticated -> ShowtimeHome()
        }
    }
}
