package rs.edu.raf.rma.showtime.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text("Profile", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        when {
            state.isLoading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) { CircularProgressIndicator() }
            }

            state.error != null -> {
                Text(state.error!!, color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.height(8.dp))
                TextButton(onClick = { viewModel.setEvent(ProfileContract.UiEvent.Retry) }) {
                    Text("Retry")
                }
            }

            else -> {
                InfoRow("Name", state.fullName)
                InfoRow("Username", "@${state.username}")
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Library", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Favorites", state.favoritesCount.toString())
                InfoRow("Watchlist", state.watchlistCount.toString())
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Quiz stats", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.padding(16.dp)) {
                InfoRow("Best score", state.bestScore?.let { formatScore(it) } ?: "—")
                InfoRow("Quizzes played", state.gamesPlayed.toString())
            }
        }

        Spacer(Modifier.weight(1f))
        Button(
            onClick = { viewModel.setEvent(ProfileContract.UiEvent.Logout) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828)),
        ) { Text("Log out") }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

private fun formatScore(score: Double): String {
    val rounded = (score * 100).toLong()
    val whole = rounded / 100
    val frac = (rounded % 100).toInt()
    val fracStr = if (frac < 10) "0$frac" else "$frac"
    return "$whole.$fracStr"
}
