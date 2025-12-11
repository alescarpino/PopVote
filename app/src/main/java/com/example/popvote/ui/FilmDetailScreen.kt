
package com.example.popvote.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.popvote.viewmodel.PopVoteViewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Folder
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.compose.runtime.saveable.Saver
import com.example.popvote.viewmodel.PopVoteViewModel.MoveResult


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FilmDetailScreen(
    viewModel: PopVoteViewModel,
    filmId: String,
    onBack: () -> Unit,
) {
    // 1) Read the film by id (null-safe)
    val film = remember(filmId, viewModel.allFilms, viewModel.folders) {
        viewModel.getFilmById(filmId)
    }

    // 2) Handle missing film gracefully
    if (film == null) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text("Film not found") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    }
                )
            }
        ) { padding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Text("This film was removed or does not exist.")
            }
        }
        return
    }

    // Saver to persist Uri? as String? across configuration changes
    val uriSaver: Saver<Uri?, String> = Saver(
        save = { uri: Uri? -> uri?.toString() ?: "" },
        restore = { saved: String -> saved.takeIf { it.isNotEmpty() }?.let(Uri::parse) }
    )
    // Editable cover image Uri (saveable with custom saver)
    var imageUri: Uri? by rememberSaveable(filmId, stateSaver = uriSaver) {
        mutableStateOf(film.imageUri) // 'film' is non-null here
    }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) imageUri = uri
    }

    // --- Editable state bound to the current film ---
    var title by rememberSaveable(filmId) { mutableStateOf(film.title) }
    var description by rememberSaveable(filmId) { mutableStateOf(film.description) }

    // Genre (enum) + dropdown state
    var genreExpanded by rememberSaveable(filmId) { mutableStateOf(false) }
    val genreOptions = remember { com.example.popvote.model.Genre.values().toList() }
    var genre by rememberSaveable(filmId) { mutableStateOf(film.genre) }

    // Rating (1..5)
    var rating by rememberSaveable(filmId) { mutableStateOf(film.rating) }

    // Duration as text for validation
    var durationText by rememberSaveable(filmId) { mutableStateOf(film.duration.toString()) }

    // Folder selection for "Move"
    var moveDialogOpen by rememberSaveable { mutableStateOf(false) }
    val folders = viewModel.folders // assume list of Folder(id, name, ...)

// Take the first folder (or null if there are no folders) as initial selection
    var targetFolderId: String? by rememberSaveable(filmId) {
        mutableStateOf<String?>(folders.firstOrNull()?.id)
    }

    // Snackbar + coroutine scope
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // --- Helper: parse and validate duration safely ---
    fun parseDurationOrNull(text: String): Int? =
        text.toIntOrNull()?.takeIf { it >= 1 }

    // --- SAVE action: validate -> build updated film -> call VM -> snackbar + back ---
    fun onSave() {
        val trimmedTitle = title.trim()
        val trimmedDescription = description.trim()
        val parsedDuration = parseDurationOrNull(durationText)

        // basic validation
        val errorMsg = when {
            trimmedTitle.isEmpty() -> "Title cannot be empty."
            parsedDuration == null -> "Duration must be a positive number (min >= 1)."
            else -> null
        }

        if (errorMsg != null) {
            scope.launch { snackbarHostState.showSnackbar(errorMsg) }
            return
        }

        val updated = film.copy(
            title = trimmedTitle,
            description = trimmedDescription,
            genre = genre,
            rating = rating.coerceIn(1, 5),
            duration = parsedDuration!!,
            imageUri = imageUri,
        )

        // Call into VM
        viewModel.updateFilm(updated)

        scope.launch {
            snackbarHostState.showSnackbar("Saved changes.")
        }
        // Optional: navigate back after save
        onBack()
    }

    // --- DELETE action: confirm dialog then delete via VM ---
    var deleteConfirmOpen by rememberSaveable { mutableStateOf(false) }
    fun onDeleteConfirmed() {
        viewModel.deleteFilm(film.id)
        scope.launch {
            snackbarHostState.showSnackbar("Film deleted.")
        }
        onBack()
    }

    // --- MOVE action: open dialog to pick target folder, then update on confirm ---
    fun onMove() {
        moveDialogOpen = true
    }

    fun onMoveConfirm() {
        // 1) ensure a target folder was selected
        val targetId = targetFolderId
        if (targetId == null) {
            scope.launch { snackbarHostState.showSnackbar("Please select a target folder.") }
            return
        }

        // 2) delegate the actual move to the ViewModel
        val result = viewModel.moveFilmToFolder(
            filmId = film.id,
            targetFolderId = targetId
        )

        // 3) give feedback based on the outcome
        scope.launch {
            when (result) {
                MoveResult.Moved -> snackbarHostState.showSnackbar("Film moved.")
                MoveResult.NoChange -> snackbarHostState.showSnackbar("Film already in selected folder.")
                MoveResult.SourceNotFound -> snackbarHostState.showSnackbar("Current folder not found.")
                MoveResult.TargetNotFound -> snackbarHostState.showSnackbar("Target folder not found.")
                MoveResult.FilmNotFound -> snackbarHostState.showSnackbar("Film not found in any folder.")
                MoveResult.Error -> snackbarHostState.showSnackbar("Could not move film.")
            }
        }

        // 4) close dialog in all cases (    // 4) close dialog in all cases (optional: only on success)
        moveDialogOpen = false
    }

        // 3) Editable UI + top bar actions
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(film.title) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    // Save
                    IconButton(onClick = { onSave() }) {
                        Icon(Icons.Default.Save, contentDescription = "Save")
                    }
                    // Move
                    IconButton(onClick = { onMove() }) {
                        Icon(Icons.Default.Folder, contentDescription = "Move to folder")
                    }
                    // Delete
                    IconButton(onClick = { deleteConfirmOpen = true }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image (cover) - picker area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.LightGray)
                    .clickable { imagePicker.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = "Film cover",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Text("Tap to add/replace cover")
                }
            }

            // Title (editable)
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            // Description (editable)
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Description") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )

            // Genre dropdown
            ExposedDropdownMenuBox(
                expanded = genreExpanded,
                onExpandedChange = { genreExpanded = !genreExpanded }
            ) {
                OutlinedTextField(
                    value = genre.name,
                    onValueChange = {},
                    readOnly = true,
                    label = { Text("Genre") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = genreExpanded) },
                    modifier = Modifier.menuAnchor().fillMaxWidth()
                )
                ExposedDropdownMenu(
                    expanded = genreExpanded,
                    onDismissRequest = { genreExpanded = false }
                ) {
                    genreOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option.name) },
                            onClick = {
                                genre = option
                                genreExpanded = false
                            }
                        )
                    }
                }
            }

            // Rating via slider (1..5)
            Column {
                Text("Rating: $rating/5")
                Slider(
                    value = rating.toFloat(),
                    onValueChange = { rating = it.toInt().coerceIn(1, 5) },
                    valueRange = 1f..5f,
                    steps = 3
                )
            }

            // Duration numeric input
            OutlinedTextField(
                value = durationText,
                onValueChange = { input ->
                    // keep only digits; validation happens on save
                    durationText = input.filter { it.isDigit() }
                },
                label = { Text("Duration (min)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number,
                    imeAction = ImeAction.Done
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // --- Delete confirmation dialog ---
    if (deleteConfirmOpen) {
        AlertDialog(
            onDismissRequest = { deleteConfirmOpen = false },
            confirmButton = {
                TextButton(onClick = {
                    deleteConfirmOpen = false
                    onDeleteConfirmed()
                }) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { deleteConfirmOpen = false }) { Text("Cancel") }
            },
            icon = { Icon(Icons.Default.Delete, contentDescription = null) },
            title = { Text("Delete film") },
            text = { Text("Are you sure you want to delete '${film.title}'? This action cannot be undone.") }
        )
    }

    // --- Move dialog: pick target folder ---
    if (moveDialogOpen) {
        AlertDialog(
            onDismissRequest = { moveDialogOpen = false },
            confirmButton = {
                TextButton(onClick = { onMoveConfirm() }) { Text("Move") }
            },
            dismissButton = {
                TextButton(onClick = { moveDialogOpen = false }) { Text("Cancel") }
            },
            icon = { Icon(Icons.Default.Folder, contentDescription = null) },
            title = { Text("Move to folder") },
            text = {
                // Simple radio list of folders
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    folders.forEach { folder ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { targetFolderId = folder.id }
                                .padding(vertical = 4.dp)
                        ) {
                            RadioButton(
                                selected = targetFolderId == folder.id,
                                onClick = { targetFolderId = folder.id }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(folder.name)
                        }
                    }
                }
            }
        )
    }
}

