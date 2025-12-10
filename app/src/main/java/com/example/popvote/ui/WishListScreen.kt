package com.example.popvote.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.popvote.model.Folder
import com.example.popvote.model.Genre
import com.example.popvote.model.Wish
import com.example.popvote.viewmodel.PopVoteViewModel
import androidx.compose.material3.ExperimentalMaterial3Api
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WishlistScreen(viewModel: PopVoteViewModel, onBack: () -> Unit) {
    val wishlist = viewModel.wishlist
    // CORRETTO: Uso 'folders' come nel tuo ViewModel
    val userFolders = viewModel.folders

    var selectedWish by remember { mutableStateOf<Wish?>(null) }
    var showConvertDialog by remember { mutableStateOf(false) }
    var showAddDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Wishlist 📝") },
                navigationIcon = {
                    Button(onClick = onBack) { Text("<") }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Color(0xFF03DAC5)
            ) {
                Text("+", fontSize = 24.sp, color = Color.White)
            }
        }
    ) { padding ->
        if (wishlist.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("Nessun film in lista!", color = Color.Gray)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(wishlist) { wish ->
                    WishListFilmCard(
                        wish = wish,
                        // CORRETTO: Chiamo removeFilmFromWishlist
                        onDelete = { viewModel.removeFilmFromWishlist(wish) },
                        onConvert = {
                            selectedWish = wish
                            showConvertDialog = true
                        }
                    )
                }
            }
        }
    }

    // DIALOG AGGIUNTA
    if (showAddDialog) {
        AddWishDialog(
            onDismiss = { showAddDialog = false },
            onConfirm = { title, uri ->
                // CORRETTO: Chiamo addFilmToWishlist
                viewModel.addFilmToWishlist(
                    title = title,
                    description = "", // Vuoto temporaneo
                    genre = Genre.ACTION, // Finto temporaneo
                    duration = 0, // Finto temporaneo
                    imageUri = uri
                )
                showAddDialog = false
            }
        )
    }

    // DIALOG CONVERSIONE
    if (showConvertDialog && selectedWish != null) {
        ConvertWishDialog(
            userFolders = userFolders,
            initialTitle = selectedWish!!.title,
            initialImageUri = selectedWish!!.imageUri,
            onDismiss = { showConvertDialog = false },
            onConfirm = { title, desc, genre, folderId, rating, duration, uri ->

                // CORRETTO: Chiamo addFilmToFolder
                viewModel.addFilmToFolder(
                    folderId = folderId,
                    title = title,
                    description = desc,
                    genre = genre,
                    rating = rating,
                    duration = duration,
                    imageUri = uri
                )

                // CORRETTO: Chiamo removeFilmFromWishlist
                viewModel.removeFilmFromWishlist(selectedWish!!)
                showConvertDialog = false
            }
        )
    }
}

// --- RESTO DEL CODICE (Card e Dialogs) ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishDialog(onDismiss: () -> Unit, onConfirm: (String, Uri?) -> Unit) {
    var title by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedImageUri = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuovo Wish ✨") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titolo") })
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(if (selectedImageUri == null) "Foto (Opzionale)" else "Foto OK")
                }
            }
        },
        confirmButton = { Button(onClick = { if (title.isNotEmpty()) onConfirm(title, selectedImageUri) }) { Text("Salva") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConvertWishDialog(
    userFolders: List<Folder>,
    initialTitle: String,
    initialImageUri: Uri?,
    onDismiss: () -> Unit,
    onConfirm: (String, String, Genre, String, Int, Int, Uri?) -> Unit
) {
    var title by remember { mutableStateOf(initialTitle) }
    var description by remember { mutableStateOf("") }
    var rating by remember { mutableStateOf(3) }
    var durationText by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf(initialImageUri) }

    var expandedGenre by remember { mutableStateOf(false) }
    var selectedGenre by remember { mutableStateOf(Genre.ACTION) }

    var expandedFolder by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf(userFolders.firstOrNull()) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { selectedImageUri = it }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Hai visto il film? 🍿") },
        text = {
            Column {
                OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Titolo") })
                Spacer(modifier = Modifier.height(8.dp))

                // Selezione Genere (Action, Horror...)
                ExposedDropdownMenuBox(expanded = expandedGenre, onExpandedChange = { expandedGenre = !expandedGenre }) {
                    OutlinedTextField(
                        readOnly = true, value = selectedGenre.name, onValueChange = {}, label = { Text("Genere Film") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedGenre) }, modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedGenre, onDismissRequest = { expandedGenre = false }) {
                        Genre.entries.forEach { genre ->
                            DropdownMenuItem(text = { Text(genre.name) }, onClick = { selectedGenre = genre; expandedGenre = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))

                // Selezione Cartella (Folder Utente)
                ExposedDropdownMenuBox(expanded = expandedFolder, onExpandedChange = { expandedFolder = !expandedFolder }) {
                    OutlinedTextField(
                        readOnly = true, value = selectedFolder?.name ?: "Scegli Cartella", onValueChange = {}, label = { Text("Dove lo salvo?") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedFolder) }, modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = expandedFolder, onDismissRequest = { expandedFolder = false }) {
                        userFolders.forEach { folder ->
                            DropdownMenuItem(text = { Text(folder.name) }, onClick = { selectedFolder = folder; expandedFolder = false })
                        }
                    }
                }
                if(userFolders.isEmpty()) Text("Crea prima una cartella nella Home!", color = Color.Red, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Descrizione") })
                OutlinedTextField(value = durationText, onValueChange = { durationText = it.filter { c -> c.isDigit() } }, label = { Text("Durata (min)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number))
                Text("Voto: $rating/5")
                Slider(value = rating.toFloat(), onValueChange = { rating = it.toInt() }, valueRange = 1f..5f, steps = 3)

                Button(onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(if (selectedImageUri == null) "Locandina" else "Locandina OK")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val duration = durationText.toIntOrNull() ?: 0
                    if (title.isNotEmpty() && selectedFolder != null) {
                        onConfirm(title, description, selectedGenre, selectedFolder!!.id, rating, duration, selectedImageUri)
                    }
                },
                enabled = selectedFolder != null
            ) { Text("Archivia") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}