package rs.edu.raf.rma.movie.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import rs.edu.raf.rma.movie.data.model.FilterParams
import rs.edu.raf.rma.movie.details.MOVIE_ID
import rs.edu.raf.rma.movie.details.MovieDetailsScreen
import rs.edu.raf.rma.movie.filter.FilterScreen
import rs.edu.raf.rma.movie.list.MovieListScreen

@Composable
fun MovieNavigation() {
    val navController = rememberNavController()
    var currentFilters by remember { mutableStateOf(FilterParams()) }
    var pendingFilters by remember { mutableStateOf<FilterParams?>(null) }

    NavHost(navController = navController, startDestination = "movies") {

        composable(route = "movies") {
            MovieListScreen(
                onMovieClick = { movieId ->
                    navController.navigate("movies/$movieId")
                },
                onFilterClick = { filters ->
                    currentFilters = filters
                    navController.navigate("filter")
                },
                pendingFilters = pendingFilters,
                onFiltersApplied = { pendingFilters = null }
            )
        }

        composable(
            route = "movies/{$MOVIE_ID}",
            arguments = listOf(navArgument(MOVIE_ID) { type = NavType.StringType })
        ) {
            MovieDetailsScreen(
                onBack = { navController.navigateUp() }
            )
        }

        composable(route = "filter") {
            FilterScreen(
                initialFilters = currentFilters,
                onApply = { filters ->
                    pendingFilters = filters
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }
    }
}
