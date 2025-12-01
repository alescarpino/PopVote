package com.example.popvote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.popvote.model.Film
import com.example.popvote.viewmodel.PopVoteViewModel

@Composable
fun AllFilmsScreen(viewModel: PopVoteViewModel) {
    val allFilms = viewModel.getAllFilmsAlphabetical()

    if (allFilms.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No films yet. Add one!", color = Color.Gray)
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(allFilms) { film ->
                FilmRow(film)
            }
        }
    }
}

@Composable
fun FilmRow(film: Film) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (film.imageUri != null) {
                AsyncImage(
                    model = film.imageUri,
                    contentDescription = null,
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .padding(8.dp)
                        .background(Color.LightGray),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Movie, contentDescription = null)
                }
            }
            Column(modifier = Modifier.padding(start = 8.dp)) {
                Text(text = film.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                Text(text = "Genre: ${film.genre}", fontSize = 14.sp, color = Color.Gray)
                Text(text = "Rating: ${film.rating}/5", fontSize = 14.sp, color = Color.Gray)
            }
        }
    }
}
