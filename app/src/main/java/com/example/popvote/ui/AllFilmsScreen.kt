
package com.example.popvote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material3.*
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllFilmsScreen(viewModel: PopVoteViewModel) {

    val allFilms = viewModel.allFilms

    val sortedFilms = remember(allFilms) {
        allFilms.sortedBy { it.title.lowercase() }
    }

    var query by rememberSaveable { mutableStateOf("") }

    val filteredFilms = remember(query, sortedFilms) {
        val q = query.trim().lowercase()
        if (q.isEmpty()) sortedFilms
        else sortedFilms.filter { f ->
            val genreText = f.genre.name.lowercase()
            f.title.lowercase().contains(q) ||
                    genreText.contains(q) ||
                    f.rating.toString().contains(q)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    TextField(
                        value = query,
                        onValueChange = { query = it },
                        singleLine = true,
                        placeholder = { Text("Look for Title, Genre, Rating…") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                        ),
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Default.Clear, contentDescription = "Delete Search")
                                }
                            }
                        }
                    )
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(
                items = filteredFilms,
                key = { it.id }
            ) { film ->
                FilmRow(film)
            }

            if (filteredFilms.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No matches for „$query“", color = Color.Gray)
                    }
                }
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
                Text(
                    text = "Genre: ${film.genre.name}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
                Text(
                    text = "Rating: ${film.rating}/5",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
        }
    }
}
