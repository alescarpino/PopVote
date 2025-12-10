package com.example.popvote.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.popvote.model.Folder
import com.example.popvote.viewmodel.PopVoteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PopVoteViewModel,
    onNavigateToGenre: (String) -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateToStatistics: () -> Unit,
    onNavigateToWishlist: () -> Unit // Questo parametro ora c'è!
) {
    // PRENDE LA LISTA DALLE "FOLDERS" (NON PIÙ GENRES)
    val folders = viewModel.folders
    var showAddFolderDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PopVote 🍿", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddFolderDialog = true },
                containerColor = Color(0xFF03DAC5)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Folder")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // --- SEZIONE BOTTONI (Ranking, Stats, Wishlist) ---
            item {
                Text("Menu Rapido", fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onNavigateToRanking,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF9800))
                    ) {
                        Text("🏆 Top")
                    }

                    Button(
                        onClick = onNavigateToStatistics,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF673AB7))
                    ) {
                        Text("📊 Stats")
                    }

                    Button(
                        onClick = onNavigateToWishlist,
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE91E63))
                    ) {
                        Text("📝 Wish")
                    }
                }
            }

            // --- SEZIONE CARTELLE (LIBRERIE) ---
            item {
                Text("Le tue Librerie", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }

            if (folders.isEmpty()) {
                item {
                    Text("Nessuna cartella. Clicca + per crearne una!", color = Color.Gray)
                }
            } else {
                items(folders) { folder ->
                    FolderCard(folder = folder, onClick = { onNavigateToGenre(folder.id) })
                }
            }
        }
    }

    if (showAddFolderDialog) {
        AddFolderDialog(
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name, uri ->
                // CHIAMA LA FUNZIONE CORRETTA DEL VIEWMODEL
                viewModel.addFolder(name, uri)
                showAddFolderDialog = false
            }
        )
    }
}

@Composable
fun FolderCard(folder: Folder, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().height(100.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (folder.imageUri != null) {
                AsyncImage(
                    model = folder.imageUri,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp),
                    contentScale = androidx.compose.ui.layout.ContentScale.Crop
                )
            } else {
                Surface(
                    modifier = Modifier.size(70.dp),
                    color = Color.LightGray
                ) {}
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(folder.name, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                Text("${folder.films.size} Film", color = Color.Gray)
            }
        }
    }
}

@Composable
fun AddFolderDialog(onDismiss: () -> Unit, onConfirm: (String, android.net.Uri?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova Cartella") },
        text = {
            Column {
                OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Nome Libreria") })
                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(if (selectedUri == null) "Scegli Immagine" else "Immagine Selezionata")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (name.isNotEmpty()) onConfirm(name, selectedUri) }) { Text("Crea") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}