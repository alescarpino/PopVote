package com.example.popvote.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import com.example.popvote.statistics.StatisticsLogic
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.popvote.viewmodel.PopVoteViewModel

@Composable
fun StatisticsScreen(viewModel: PopVoteViewModel) {
    val genres = viewModel.genres
    val logic = StatisticsLogic()

    val mostWatchedGenre = logic.getMostWatchedGenre(genres)
    val totalMinutes = logic.getTotalMinutesWatched(genres)

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Statistics", style = MaterialTheme.typography.headlineMedium)

        Spacer(modifier = Modifier.height(16.dp))
        Text("Total Minutes Watched: $totalMinutes")

        Spacer(modifier = Modifier.height(8.dp))
        if (mostWatchedGenre != null) {
            Text("Most Watched Genre: ${mostWatchedGenre.name} (${mostWatchedGenre.films.size} films)")
        } else {
            Text("No data available")
        }
    }
}
