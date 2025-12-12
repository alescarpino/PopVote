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
    val folders = viewModel.folders
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

            composable(route = "all_films") {
                AllFilmsScreen(
                    viewModel = viewModel,
                    onFilmClick = { filmId ->
                        tabNavController.navigate("film_detail/$filmId")
                    }
                )
            }

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
            userFolders = folders,
            onDismiss = { showAddFilmDialog = false },
            onConfirm = { title, desc, genre,folderId, rating, duration, uri ->

                viewModel.addFilm(id = viewModel.generateId(), title, desc, genre, rating, duration, uri)
                // if the user choose a folder, save the film there
                if (folderId != null) {
                    viewModel.addFilmToFolder(folderId, title, desc, genre, rating, duration, uri)
                }
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
                    description = "", // empty at the beginning
                    genre = com.example.popvote.model.Genre.ACTION, // Default
                    duration = 0, // 0 default
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

    //gallery launcher
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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFilmDialog(
    userFolders: List<Folder>, // Receive the folders
    onDismiss: () -> Unit,

    onConfirm: (String, String, com.example.popvote.model.Genre, String?, Int, Int, android.net.Uri?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedGenre by remember { mutableStateOf(com.example.popvote.model.Genre.ACTION) }
    var rating by remember { mutableStateOf(3) }
    var durationText by remember { mutableStateOf("115") }
    var selectedImageUri by remember { mutableStateOf<android.net.Uri?>(null) }


    var genreMenuExpanded by remember { mutableStateOf(false) }
    var folderMenuExpanded by remember { mutableStateOf(false) }
    var selectedFolder by remember { mutableStateOf<Folder?>(null) }

    val launcher = androidx.activity.compose.rememberLauncherForActivityResult(
        contract = androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia()
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


                ExposedDropdownMenuBox(
                    expanded = genreMenuExpanded,
                    onExpandedChange = { genreMenuExpanded = !genreMenuExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedGenre.name,
                        onValueChange = {},
                        label = { Text("Genre") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreMenuExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = genreMenuExpanded, onDismissRequest = { genreMenuExpanded = false }) {
                        com.example.popvote.model.Genre.entries.forEach { genre ->
                            DropdownMenuItem(
                                text = { Text(genre.name) },
                                onClick = { selectedGenre = genre; genreMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))


                ExposedDropdownMenuBox(
                    expanded = folderMenuExpanded,
                    onExpandedChange = { folderMenuExpanded = !folderMenuExpanded }
                ) {
                    OutlinedTextField(
                        readOnly = true,
                        value = selectedFolder?.name ?: "None (only All Films)",
                        onValueChange = {},
                        label = { Text("folder (Optional)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = folderMenuExpanded) },
                        modifier = Modifier.menuAnchor()
                    )
                    ExposedDropdownMenu(expanded = folderMenuExpanded, onDismissRequest = { folderMenuExpanded = false }) {
                        DropdownMenuItem(
                            text = { Text("No specific folder") },
                            onClick = { selectedFolder = null; folderMenuExpanded = false }
                        )
                        userFolders.forEach { folder ->
                            DropdownMenuItem(
                                text = { Text(folder.name) },
                                onClick = { selectedFolder = folder; folderMenuExpanded = false }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text("Rating: $rating/5")
                Slider(value = rating.toFloat(), onValueChange = { rating = it.toInt() }, valueRange = 1f..5f, steps = 3)

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = durationText, onValueChange = { durationText = it.filter { ch -> ch.isDigit() } }, label = { Text("Duration (min)") }, keyboardOptions = androidx.compose.foundation.text.KeyboardOptions.Default.copy(keyboardType = androidx.compose.ui.text.input.KeyboardType.Number))

                Spacer(modifier = Modifier.height(8.dp))
                Button(onClick = { launcher.launch(androidx.activity.result.PickVisualMediaRequest(androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly)) }) {
                    Text(if (selectedImageUri == null) "choose photo" else "photo OK")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                val duration = durationText.toIntOrNull() ?: 0
                if (title.isNotEmpty() && duration > 0) {

                    onConfirm(title, description, selectedGenre, selectedFolder?.id, rating, duration, selectedImageUri)
                }
            }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}