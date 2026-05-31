package rs.edu.raf.rma.movie.filter

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import rs.edu.raf.rma.movie.data.model.FilterParams

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilterScreen(
    initialFilters: FilterParams,
    onApply: (FilterParams) -> Unit,
    onBack: () -> Unit
) {
    val viewModel: FilterViewModel = koinViewModel()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(initialFilters) {
        viewModel.initializeFromFilters(initialFilters)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Filter Movies") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = state.query,
                onValueChange = { viewModel.processIntent(FilterIntent.SetQuery(it)) },
                label = { Text("Search by title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Text("Genre", style = MaterialTheme.typography.titleSmall)
            if (state.isLoadingGenres) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = state.selectedGenreId == null,
                        onClick = { viewModel.processIntent(FilterIntent.SelectGenre(null)) },
                        label = { Text("All") }
                    )
                    state.genres.forEach { genre ->
                        FilterChip(
                            selected = state.selectedGenreId == genre.id,
                            onClick = { viewModel.processIntent(FilterIntent.SelectGenre(genre.id)) },
                            label = { Text(genre.name) }
                        )
                    }
                }
            }

            Text("Year Range", style = MaterialTheme.typography.titleSmall)
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = state.minYear,
                    onValueChange = { viewModel.processIntent(FilterIntent.SetMinYear(it)) },
                    label = { Text("Min Year") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                OutlinedTextField(
                    value = state.maxYear,
                    onValueChange = { viewModel.processIntent(FilterIntent.SetMaxYear(it)) },
                    label = { Text("Max Year") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
            }

            Text(
                text = "Minimum Rating: ${formatOneDecimal(state.minRating)}",
                style = MaterialTheme.typography.titleSmall
            )
            Slider(
                value = state.minRating,
                onValueChange = { viewModel.processIntent(FilterIntent.SetMinRating(it)) },
                valueRange = 0f..10f,
                steps = 99,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = { viewModel.processIntent(FilterIntent.ClearAll) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Clear All")
                }
                Button(
                    onClick = { onApply(viewModel.toFilterParams()) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Apply Filters")
                }
            }
        }
    }
}

private fun formatOneDecimal(value: Float): String {
    val intPart = value.toInt()
    val decPart = ((value - intPart) * 10).toInt()
    return "$intPart.$decPart"
}
