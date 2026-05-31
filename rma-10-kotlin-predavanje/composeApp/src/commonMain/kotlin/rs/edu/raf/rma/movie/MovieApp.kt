package rs.edu.raf.rma.movie

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import rs.edu.raf.rma.movie.navigation.MovieNavigation

@Composable
fun MovieApp() {
    MaterialTheme {
        MovieNavigation()
    }
}
