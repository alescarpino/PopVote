package com.example.popvote.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.popvote.model.Folder
import com.example.popvote.viewmodel.PopVoteViewModel
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: PopVoteViewModel,
    onNavigateToGenre: (String) -> Unit,
    onNavigateToRanking: () -> Unit,
    onNavigateToStatistics: () -> Unit
) {
    // States for the Dialogues
    var showAddFolderDialog by remember { mutableStateOf(false) }
    var showAddFilmDialog by remember { mutableStateOf(false) }
    var showAddWishDialog by remember { mutableStateOf(false) }

    val tabNavController = rememberNavController()
    val navBackStackEntry by tabNavController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: "library"

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("PopVote Genres", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFF6200EE),
                    titleContentColor = Color.White
                ),
                actions = {
                    IconButton(onClick = onNavigateToRanking) {
                        Icon(Icons.Default.EmojiEvents, contentDescription = "Ranking", tint = Color.Yellow)
                    }
                    IconButton(onClick = onNavigateToStatistics) {
                        Icon(Icons.Default.BarChart, contentDescription = "Statistics", tint = Color.Green)
                    }
                }
            )
        },
        bottomBar = {
            BottomBarNav(
                navController = tabNavController,
                onAddFolderClick = { showAddFolderDialog = true },
                onAddFilmClick = { showAddFilmDialog = true },
                onAddWishClick = { showAddWishDialog = true },
            )
        }
    ) { padding ->
        NavHost(
            navController = tabNavController,
            startDestination = "library",
            modifier = Modifier.padding(padding)
        ) {
            composable("library") { /* Library */ }
            composable("all_films") { AllFilmsScreen(viewModel) }
            composable("wishlist") { WishlistScreen(viewModel) }
        }

        // library view
        Column(modifier = Modifier.padding(padding)) {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(viewModel.folders) { genre ->
                        FolderCard(
                            folder = genre,
                            currentRoute = currentRoute,
                            onClick = { onNavigateToGenre(genre.id) },
                            onDelete = { viewModel.deleteFolder(genre) }
                        )
                    }
                }

        }
    }

    if (showAddFolderDialog) {
        AddFolderDialog(
            onDismiss = { showAddFolderDialog = false },
            onConfirm = { name, uri ->
                viewModel.addFolder(name, uri)
                showAddFolderDialog = false
            }
        )
    }


    if (showAddFilmDialog) {
        AddFilmDialog(
            onDismiss = { showAddFilmDialog = false },
            onConfirm = { title, desc, genre, rating, duration, uri ->
                viewModel.addFilm(id = viewModel.generateId(), title, desc, genre, rating, duration, uri)
                showAddFilmDialog = false
            }
        )
    }

    if (showAddWishDialog) {
        AddWishDialog(
            onDismiss = { showAddWishDialog = false },
            onConfirm = { title,uri ->
                viewModel.addFilmToWishlist(
                    title = title,
                    description = "", // Vuoto per ora
                    genre = com.example.popvote.model.Genre.ACTION, // Default
                    duration = 0, // 0 per ora
                    imageUri = uri
                )
                showAddWishDialog = false
            }
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddWishDialog(
    onDismiss: () -> Unit,
    //  removed desc, genre, duration from signature
    onConfirm: (String, Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }


    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Quick Wish ✨") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") }
                )
                Spacer(modifier = Modifier.height(16.dp))

                // image button
                Button(
                    onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (selectedImageUri == null) "Select Image (Optional)" else "Image Selected")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if (title.isNotEmpty()) onConfirm(title, selectedImageUri) }) {
                Text("Add to Wishlist")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
fun FolderCard(
    folder: Folder,
    currentRoute: String,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    if (currentRoute == "library") {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clickable { onClick() },
            elevation = CardDefaults.cardElevation(4.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                if (folder.imageUri != null) {
                    AsyncImage(
                        model = folder.imageUri,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFFE0E0E0))
                    )
                }

                Text(
                    text = folder.name,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (folder.imageUri != null) Color.White else Color.Black,
                    modifier = Modifier.align(Alignment.Center)
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}

@Composable
fun AddFolderDialog(onDismiss: () -> Unit, onConfirm: (String, Uri?) -> Unit) {
    var name by remember { mutableStateOf("") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    // Launcher per la galleria
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> selectedImageUri = uri }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("New Genre") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Genre Name") }
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { launcher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Image, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (selectedImageUri == null) "Select Image" else "Image Selected")
                }
            }
        },
        confirmButton = {
            Button(onClick = { if(name.isNotEmpty()) onConfirm(name, selectedImageUri) }) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}