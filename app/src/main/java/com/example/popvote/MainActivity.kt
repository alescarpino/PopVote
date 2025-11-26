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
import com.example.popvote.ui.GenreDetailScreen
import com.example.popvote.ui.HomeScreen
import com.example.popvote.ui.RankingScreen
import com.example.popvote.viewmodel.PopVoteViewModel

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
                onNavigateToRanking = { navController.navigate("ranking") }
            )
        }

        //  detailed genres screen
        composable("genre/{genreId}") { backStackEntry ->
            val genreId = backStackEntry.arguments?.getString("genreId")
            if (genreId != null) {
                GenreDetailScreen(
                    genreId = genreId,
                    viewModel = viewModel,
                    onBack = { navController.popBackStack() }
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
    }
}