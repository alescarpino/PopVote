package com.example.popvote.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.popvote.model.Genre
import com.example.popvote.model.Wish
import com.example.popvote.viewmodel.PopVoteViewModel


@Composable
fun WishlistScreen(viewModel: PopVoteViewModel) {
    val wishlist = viewModel.wishlist
    var selectedWish by remember { mutableStateOf<Wish?>(null) }
    var showConvertDialog by remember { mutableStateOf(false) }

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
            items(wishlist) { wish ->
                WishListFilmCard(
                    wish = wish,
                    onDelete = { viewModel.removeFilmFromWishlist(wish) },
                    onConvert = {
                        selectedWish = wish
                        showConvertDialog = true
                    }
                )
            }
        }
    }
    if (showConvertDialog && selectedWish != null) {


        ConvertWishDialog(
            onDismiss = { showConvertDialog = false },
            onConfirm = { title, desc, genre, rating, duration, uri ->
                // Film hinzufügen
                viewModel.addFilm(
                    id = viewModel.generateId(),
                    title = title,
                    description = desc,
                    genre = genre,
                    rating = rating,
                    duration = duration,
                    imageUri = uri
                )
                viewModel.removeFilmFromWishlist(selectedWish!!)
                showConvertDialog = false
            },
            initialTitle = selectedWish!!.title,
            initialDescription = selectedWish!!.description,
            initialGenre = selectedWish!!.genre,
            initialDuration = selectedWish!!.duration,
            initialImageUri = selectedWish!!.imageUri
        )
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
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = genreMenuExpanded
                            )
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
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text(
                        if (selectedImageUri == null)
                            "Pick Poster"
                        else
                            "Poster Selected"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 0
                    if (title.isNotEmpty() && duration > 0) {
                        onConfirm(title, description, selectedGenre, duration, selectedImageUri)
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertWishDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String, Genre, Int, Int, Uri?) -> Unit,
    initialTitle: String = "",
    initialDescription: String = "",
    initialGenre: Genre = Genre.ACTION,
    initialRating: Int = 3,
    initialDuration: Int = 0,
    initialImageUri: Uri? = null
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf(initialDescription) }
    var selectedGenre by remember { mutableStateOf(initialGenre) }
    var rating by remember { mutableStateOf(initialRating) }
    var durationText by remember { mutableStateOf(initialDuration.toString()) }
    var selectedImageUri by remember { mutableStateOf(initialImageUri) }
    var genreMenuExpanded by remember { mutableStateOf(false) }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Convert Wish to Film") },
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
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = genreMenuExpanded
                            )
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

                Text("Rating: $rating/5")

                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt() },
                    valueRange = 1f..5f,
                    steps = 3
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = durationText,
                    onValueChange = { durationText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Duration (minutes)") },
                    keyboardOptions = KeyboardOptions.Default.copy(
                        keyboardType = KeyboardType.Number
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Button(
                    onClick = {
                        launcher.launch(
                            PickVisualMediaRequest(
                                ActivityResultContracts.PickVisualMedia.ImageOnly
                            )
                        )
                    }
                ) {
                    Text(
                        if (selectedImageUri == null)
                            "Pick Poster"
                        else
                            "Poster Selected"
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 0
                    if (title.isNotEmpty() && duration > 0) {
                        onConfirm(
                            title,
                            description,
                            selectedGenre,
                            rating,
                            duration,
                            selectedImageUri
                        )
                    }
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
