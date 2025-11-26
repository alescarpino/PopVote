package com.example.popvote.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Image
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
fun GenreDetailScreen(
    genreId: String,
    viewModel: PopVoteViewModel,
    onBack: () -> Unit
) {
    val genre = viewModel.getGenre(genreId)
    var showAddFilmDialog by remember { mutableStateOf(false) }

    if (genre == null) {
        Text("Genre not found")
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(genre.name) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFBB86FC))
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddFilmDialog = true }, containerColor = Color(0xFF03DAC5)) {
                Icon(Icons.Default.Add, contentDescription = "Add Film")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(genre.films) { film ->
                FilmCard(film = film, onDelete = { viewModel.deleteFilmFromGenre(genreId, film) })
            }
        }
    }

    if (showAddFilmDialog) {
        AddFilmDialog(
            onDismiss = { showAddFilmDialog = false },
            onConfirm = { title, desc, rating, uri ->
                viewModel.addFilmToGenre(genreId, title, desc, rating, uri)
                showAddFilmDialog = false
            }
        )
    }
}

@Composable
fun FilmCard(film: Film, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // film image
            if (film.imageUri != null) {
                AsyncImage(
                    model = film.imageUri,
                    contentDescription = "Cover",
                    modifier = Modifier.size(80.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Surface(modifier = Modifier.size(80.dp), color = Color.LightGray, shape = RoundedCornerShape(8.dp)) {}
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(film.title, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(film.description, fontSize = 14.sp, color = Color.Gray, maxLines = 2)

                // valutation with stars
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

@Composable
fun AddFilmDialog(onDismiss: () -> Unit, onConfirm: (String, String, Int, Uri?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(3) } // Default 3 stelle
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Film") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Title") })
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Description") })
                Spacer(modifier = Modifier.height(8.dp))

                Text("Rating: $rating/5")
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Button(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(if (selectedImageUri == null) "Pick Poster" else "Poster Selected")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if(title.isNotEmpty()) onConfirm(title, description, rating, selectedImageUri) }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}