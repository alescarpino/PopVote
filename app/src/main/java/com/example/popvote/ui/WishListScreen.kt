
package com.example.popvote.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
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
import com.example.popvote.viewmodel.PopVoteViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.input.KeyboardType
import com.example.popvote.model.Genre
import com.example.popvote.model.Wish

@Composable
fun WishlistScreen(viewModel: PopVoteViewModel) {
    val wishlist = viewModel.wishlist

    if (wishlist.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Your wishlist is empty!",
                color = Color.Gray,
                fontSize = 18.sp
            )
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(wishlist) { film ->
                WishListFilmCard(
                    film = film,
                    onDelete = { viewModel.removeFilmFromWishlist(film) }
                )
            }
        }
    }
}

@Composable
fun WishListFilmCard(film: Wish, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
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
                Text(
                    text = film.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = film.description,
                    fontSize = 14.sp,
                    color = Color.Gray,
                    maxLines = 2
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = "Delete",
                    tint = Color.Red
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Genre, Int, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(Genre.ACTION) }
    var durationText by remember { mutableStateOf("115") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var genreMenuExpanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Wish") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") }
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Genre Dropdown
                ExposedDropdownMenuBox(
                    expanded = genreMenuExpanded,
                    onExpandedChange = { genreMenuExpanded = !genreMenuExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedGenre.name,
                        onValueChange = {},
                        label = { Text("Genre") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreMenuExpanded)
                        },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(
                        expanded = genreMenuExpanded,
                        onDismissRequest = { genreMenuExpanded = false }
                    ) {
                        Genre.entries.forEach { genre ->
                            DropdownMenuItem(
                                text = { Text(genre.name) },
                                onClick = {
                                    selectedGenre = genre
                                    genreMenuExpanded = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = {
                    launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                }) {
                    Text(if (selectedImageUri == null) "Pick Poster" else "Poster Selected")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val duration = durationText.toIntOrNull() ?: 0
                if (title.isNotEmpty() && duration > 0) {
                    onConfirm(title, description, selectedGenre, duration, selectedImageUri)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
