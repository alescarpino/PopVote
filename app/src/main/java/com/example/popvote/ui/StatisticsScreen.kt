
package com.example.popvote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.popvote.statistics.StatisticsLogic
import com.example.popvote.viewmodel.PopVoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(viewModel: PopVoteViewModel, onBack: () -> Unit) {
    val genres = viewModel.folders
    val logic = StatisticsLogic()

    val mostWatchedGenre = logic.getMostWatchedGenre(genres)
    val totalMinutes = logic.getTotalMinutesWatched(genres)

    val stats = listOf(
        "Total Minutes Watched " to "$totalMinutes",
        "Most Watched Genre " to (mostWatchedGenre?.name ?: "No data") +
                if (mostWatchedGenre != null) " (${mostWatchedGenre.films.size} films)" else ""
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics 📊") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stats) { (title, value) ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text(value, color = Color.DarkGray, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}

