package rs.edu.raf.rma.movie.details

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movie.data.model.Movie
import rs.edu.raf.rma.movie.data.model.MovieImage
import rs.edu.raf.rma.movie.data.model.PersonSummary

private const val IMAGE_BACKDROP = "https://image.tmdb.org/t/p/w780"
private const val IMAGE_POSTER = "https://image.tmdb.org/t/p/w342"
private const val IMAGE_PROFILE = "https://image.tmdb.org/t/p/w185"

@Composable
fun MovieDetailsScreen(onBack: () -> Unit) {
    val viewModel: MovieDetailsViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()
    val uriHandler = LocalUriHandler.current

    when {
        state.isLoading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        }
        state.error != null -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(state.error!!, color = MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = { viewModel.processIntent(MovieDetailsIntent.Retry) }) {
                        Text("Retry")
                    }
                }
            }
        }
        state.movie != null -> {
            val movie = state.movie!!
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Backdrop + back button
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    AsyncImage(
                        model = movie.backdropPath?.let { "$IMAGE_BACKDROP$it" },
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .padding(top = 40.dp, start = 8.dp)
                            .align(Alignment.TopStart)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    if (state.trailerKey != null) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                            modifier = Modifier
                                .size(56.dp)
                                .align(Alignment.Center)
                        ) {
                            IconButton(
                                onClick = {
                                    uriHandler.openUri("https://www.youtube.com/watch?v=${state.trailerKey}")
                                }
                            ) {
                                Icon(
                                    Icons.Default.PlayArrow,
                                    contentDescription = "Watch Trailer",
                                    tint = Color.White,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                }

                // Poster + title row
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .offset(y = (-40).dp)
                ) {
                    AsyncImage(
                        model = movie.posterPath?.let { "$IMAGE_POSTER$it" },
                        contentDescription = movie.title,
                        modifier = Modifier
                            .width(100.dp)
                            .height(150.dp)
                            .clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(Modifier.width(16.dp))
                    Column(
                        modifier = Modifier
                            .padding(top = 48.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = movie.title,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = listOfNotNull(
                                movie.year?.toString(),
                                movie.runtime?.let { "${it} min" }
                            ).joinToString(" · "),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .offset(y = (-40).dp)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Ratings
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (movie.imdbRating != null) {
                            Icon(
                                Icons.Default.Star, null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(
                                movie.imdbRating.toString(),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodyLarge
                            )
                            movie.imdbVotes?.let {
                                Text(
                                    " (${formatVotes(it)} votes)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        if (movie.tmdbRating != null) {
                            Spacer(Modifier.width(16.dp))
                            Text(
                                "TMDB: ${movie.tmdbRating}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Genres
                    if (movie.genres.isNotEmpty()) {
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            movie.genres.forEach { genre ->
                                SuggestionChip(onClick = {}, label = { Text(genre.name) })
                            }
                        }
                    }

                    // Overview
                    if (!movie.overview.isNullOrEmpty()) {
                        Text(movie.overview, style = MaterialTheme.typography.bodyMedium)
                    }

                    // Info badges
                    InfoBadges(movie)

                    // Backdrop images gallery
                    if (state.images.isNotEmpty()) {
                        Text(
                            "Images",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            state.images.forEach { image ->
                                BackdropImage(image)
                            }
                        }
                    }

                    // Cast
                    if (state.cast.isNotEmpty()) {
                        Text(
                            "Cast",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        state.cast.forEach { person ->
                            CastItem(person)
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

@Composable
private fun InfoBadges(movie: Movie) {
    val items = buildList {
        movie.budget?.takeIf { it > 0 }?.let { add("Budget" to formatMoney(it)) }
        movie.revenue?.takeIf { it > 0 }?.let { add("Revenue" to formatMoney(it)) }
        movie.languageCode?.let { add("Language" to it.uppercase()) }
        movie.popularity?.let { add("Popularity" to formatOneDecimal(it)) }
    }
    if (items.isEmpty()) return

    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items.forEach { (label, value) ->
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        label,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun BackdropImage(image: MovieImage) {
    AsyncImage(
        model = "$IMAGE_BACKDROP${image.filePath}",
        contentDescription = null,
        modifier = Modifier
            .width(240.dp)
            .height(135.dp)
            .clip(RoundedCornerShape(8.dp)),
        contentScale = ContentScale.Crop
    )
}

@Composable
private fun CastItem(person: PersonSummary) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        AsyncImage(
            model = person.profilePath?.let { "$IMAGE_PROFILE$it" },
            contentDescription = person.name,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                person.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            person.professions?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun formatVotes(votes: Int): String = when {
    votes >= 1_000_000 -> "${votes / 1_000_000}M"
    votes >= 1_000 -> "${votes / 1_000}K"
    else -> votes.toString()
}

private fun formatMoney(amount: Long): String = when {
    amount >= 1_000_000_000 -> "$${amount / 1_000_000_000}B"
    amount >= 1_000_000 -> "$${amount / 1_000_000}M"
    amount >= 1_000 -> "$${amount / 1_000}K"
    else -> "$$amount"
}

private fun formatOneDecimal(value: Float): String {
    val intPart = value.toInt()
    val decPart = ((value - intPart) * 10).toInt()
    return "$intPart.$decPart"
}
