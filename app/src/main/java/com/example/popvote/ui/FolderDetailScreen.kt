
package com.example.popvote.ui


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
fun FolderDetailScreen(
    folderId: String,
    viewModel: PopVoteViewModel,
    onBack: () -> Unit
) {
    val folder = viewModel.getFolder(folderId)

    if (folder == null) {
        Text("Genre not found")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(folder.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFBB86FC))
            )
        },
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(folder.films) { film ->
                FilmCard(
                    film = film,
                    onDelete = { viewModel.deleteFilmFromFolder(folderId, film) }
                )
            }
        }
    }

}

@Composable
fun FilmCard(
    film: Film,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Film Cover
            if (film.imageUri != null) {
                AsyncImage(
                    model = film.imageUri,
                    contentDescription = "Cover",
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(80.dp),
                    color = Color.LightGray,
                    shape = RoundedCornerShape(8.dp)
                ) {}
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(film.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(
                    film.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )

                // Genre
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Genre: ${film.genre.name}",
                    fontSize = 12.sp,
                    color = Color(0xFF6D6D6D)
                )

                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = if (index < film.rating) Icons.Default.Star else Icons.Outlined.StarBorder,
                            contentDescription = null,
                            tint = Color(0xFFFFD700),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, tint = Color.Red, contentDescription = "Delete")
            }
        }
    }
}
