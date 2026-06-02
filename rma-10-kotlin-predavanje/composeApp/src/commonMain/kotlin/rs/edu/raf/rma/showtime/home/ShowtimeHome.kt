package rs.edu.raf.rma.showtime.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import rs.edu.raf.rma.movie.MovieApp
import rs.edu.raf.rma.showtime.profile.ProfileScreen
import rs.edu.raf.rma.showtime.quiz.QuizScreen

private enum class ShowtimeTab(val label: String, val icon: ImageVector) {
    Movies("Movies", Icons.Default.Movie),
    Quiz("Quiz", Icons.Default.Quiz),
    Profile("Profile", Icons.Default.Person),
}

@Composable
fun ShowtimeHome() {
    var selectedTab by remember { mutableStateOf(ShowtimeTab.Movies) }

    Scaffold(
        bottomBar = {
            NavigationBar {
                ShowtimeTab.entries.forEach { tab ->
                    NavigationBarItem(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.label) },
                        label = { Text(tab.label) },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = padding.calculateBottomPadding()),
        ) {
            when (selectedTab) {
                ShowtimeTab.Movies -> MovieApp()
                ShowtimeTab.Quiz -> QuizScreen(onExit = { selectedTab = ShowtimeTab.Movies })
                ShowtimeTab.Profile -> ProfileScreen()
            }
        }
    }
}
