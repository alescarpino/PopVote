package com.example.popvote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.popvote.ui.FolderDetailScreen
import com.example.popvote.ui.HomeScreen
import com.example.popvote.ui.RankingScreen
import com.example.popvote.ui.StatisticsScreen
import com.example.popvote.viewmodel.PopVoteViewModel
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.popvote.ui.AllFilmsScreen
import com.example.popvote.ui.FilmDetailScreen


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PopVoteApp()
                }
            }
        }
    }
}

@Composable
fun PopVoteApp() {
    val navController = rememberNavController()
    val viewModel: PopVoteViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {

        //  Home Page (Genres)
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onNavigateToGenre = { genreId -> navController.navigate("genre/$genreId") },
                onNavigateToRanking = { navController.navigate("ranking") },
                onNavigateToStatistics = { navController.navigate("statistics") },
            )
        }

        //  detailed genres screen
        composable("genre/{genreId}") { backStackEntry ->
            val folderId = backStackEntry.arguments?.getString("genreId")
            if (folderId != null) {
                FolderDetailScreen(
                    folderId = folderId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() },
                    onFilmClick = { filmId -> navController.navigate("film/$filmId") },
                )
            }
        }

        // ranking screen
        composable("ranking") {
            RankingScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
        composable("statistics") {
            StatisticsScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }

        //Film detail route (navigate by film id)
        composable(
            route = "film/{filmId}",
            arguments = listOf(navArgument("filmId") { type = NavType.StringType })
        ) { backStackEntry ->
            // Read the filmId from the route arguments
            val filmId = backStackEntry.arguments?.getString("filmId") ?: return@composable

            // Show the detail screen
            FilmDetailScreen(
                viewModel = viewModel,
                filmId = filmId,
                onBack = { navController.popBackStack() }
            )

        }

        // All films route: shows the full list of films and lets you tap to open details
        composable("all_films") {
            AllFilmsScreen(
                viewModel = viewModel,
                onFilmClick = { filmId ->
                    // Navigate to the film details route by id
                    navController.navigate("film/$filmId")
                }
            )
        }

    }
}
