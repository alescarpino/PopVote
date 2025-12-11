
package com.example.popvote.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.popvote.data.StorageManager
import com.example.popvote.model.Film
import com.example.popvote.model.Folder
import com.example.popvote.model.Genre
import com.example.popvote.model.Wish
import java.util.UUID

class PopVoteViewModel(application: Application) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    private val _folders = mutableStateListOf<Folder>()
    val folders: List<Folder> get() = _folders

    private val _allFilms = mutableStateListOf<Film>()
    val allFilms: List<Film> get() = _allFilms
    private val _wishlist = mutableStateListOf<Wish>()
    val wishlist: List<Wish> get() = _wishlist

    init {
        loadData()
    }


    private fun loadData() {
        val (folders, films, wishlistFilms) = storageManager.loadAll()
        _folders.clear()
        _folders.addAll(folders)
        _allFilms.clear()
        _allFilms.addAll(films)
        _wishlist.clear()
        _wishlist.addAll(wishlistFilms)
    }


    private fun saveData() {
        storageManager.saveAll(_folders.toList(), _allFilms.toList(), _wishlist.toList())

    }

    fun generateId(): String {
        return UUID.randomUUID().toString()
    }

    fun addFolder(name: String, imageUri: Uri?) {
        val savedUri = imageUri?.let { storageManager.copyImageToInternalStorage(it) }
        _folders.add(Folder(id = UUID.randomUUID().toString(), name = name, imageUri = savedUri))
        saveData()
    }

    fun deleteFolder(folder: Folder) {
        _folders.remove(folder)
        saveData()
    }

    fun addFilmToFolder(
        folderId: String,
        title: String,
        description: String,
        genre: Genre,
        rating: Int,
        duration: Int,
        imageUri: Uri?
    ) {
        val folder = _folders.find { it.id == folderId } ?: return
        val savedUri = imageUri?.let { storageManager.copyImageToInternalStorage(it) }

        val newFilm = Film(
            title = title,
            description = description,
            genre = genre,
            rating = rating,
            duration = duration,
            imageUri = savedUri
        )

        folder.films.add(newFilm)
        val idx = _folders.indexOf(folder)
        if (idx != -1) _folders[idx] = folder.copy()

        saveData()
    }

    fun deleteFilmFromFolder(folderId: String, film: Film) {
        val folder = _folders.find { it.id == folderId } ?: return
        folder.films.remove(film)
        val idx = _folders.indexOf(folder)
        if (idx != -1) _folders[idx] = folder.copy()
        saveData()
    }


    // Get a film by id from allFilms or any folder
    fun getFilmById(id: String): Film? {
        // First try allFilms
        _allFilms.find { it.id == id }?.let { return it }
        // Then search every folder
        _folders.forEach { folder ->
            folder.films.find { it.id == id }?.let { return it }
        }
        return null
    }

    /**
     * Update a film across the app:
     * - If the film exists in allFilms, replace it there.
     * - If the film exists in any folder, replace it there too.
     * This keeps data consistent even if the same film also lives in folders.
     */
    fun updateFilm(updated: Film) {
        // Optionally copy the image into internal storage (align with your add methods)
        val finalImageUri = updated.imageUri?.let { storageManager.copyImageToInternalStorage(it) } ?: updated.imageUri
        val toWrite = updated.copy(imageUri = finalImageUri)

        // Replace in allFilms
        val idxAll = _allFilms.indexOfFirst { it.id == toWrite.id }
        if (idxAll != -1) {
            _allFilms[idxAll] = toWrite
        }

        // Replace in any folder that contains this film
        _folders.forEachIndexed { folderIndex, folder ->
            val filmIndex = folder.films.indexOfFirst { it.id == toWrite.id }
            if (filmIndex != -1) {
                val newFilms = folder.films.toMutableList()
                newFilms[filmIndex] = toWrite
                _folders[folderIndex] = folder.copy(films = newFilms)
            }
        }

        saveData()
    }

    /**
     * Delete a film everywhere:
     * - Remove from allFilms.
     * - Remove from any folder that contains it.
     */
    fun deleteFilm(filmId: String) {
        _allFilms.removeAll { it.id == filmId }

        _folders.forEachIndexed { folderIndex, folder ->
            val newFilms = folder.films.filterNot { it.id == filmId }
            if (newFilms.size != folder.films.size) {
                _folders[folderIndex] = folder.copy(films = newFilms as MutableList<Film>)
            }
        }

        saveData()
    }

    /**
     * Add an existing film (by id) to a folder WITHOUT creating a new id.
     * This avoids duplicates with different ids.
     */
    fun addExistingFilmToFolder(folderId: String, filmId: String) {
        val folder = _folders.find { it.id == folderId } ?: return
        val film = getFilmById(filmId) ?: return

        // Don't add twice
        if (folder.films.any { it.id == filmId }) return

        val newFilms = folder.films.toMutableList().apply { add(film) }
        val idx = _folders.indexOf(folder)
        if (idx != -1) _folders[idx] = folder.copy(films = newFilms)

        saveData()
    }

    /**
     * Move a film to a different folder:
     * - Remove it from any folder it currently lives in.
     * - Add it to the target folder.
     * Film id stays the same.
     */
    fun moveFilmToFolder(filmId: String, targetFolderId: String) {
        val target = _folders.find { it.id == targetFolderId } ?: return
        val film = getFilmById(filmId) ?: return

        // Remove from all folders
        _folders.forEachIndexed { folderIndex, folder ->
            if (folder.films.any { it.id == filmId }) {
                val newFilms = folder.films.filterNot { it.id == filmId }
                _folders[folderIndex] = folder.copy(films = newFilms as MutableList<Film>)
            }
        }

        // Add to target folder
        val targetIndex = _folders.indexOf(target)
        if (targetIndex != -1) {
            val targetFilms = _folders[targetIndex].films.toMutableList().apply { add(film) }
            _folders[targetIndex] = target.copy(films = targetFilms)
        }

        saveData()
    }

    fun getAllFilmsRanked(): List<Film> {
        return _allFilms.sortedByDescending { it.rating }
    }

    fun getFolder(id: String): Folder? {
        return _folders.find { it.id == id }
    }



    fun addFilm(
        id: String,
        title: String,
        description: String,
        genre: Genre,
        rating: Int,
        duration: Int,
        imageUri: Uri?
    ) {
        val savedUri = imageUri?.let { storageManager.copyImageToInternalStorage(it) }
        val newFilm = Film(id,title, description, genre, rating, duration, savedUri)
        _allFilms.add(newFilm)
        saveData()
    }


    fun addFilmToWishlist(
        title: String,
        description: String,
        genre: Genre,
        duration: Int,
        imageUri: Uri?
    ) {
        val savedUri = imageUri?.let { storageManager.copyImageToInternalStorage(it) }
        val wish = Wish(
            title = title,
            description = description,
            genre = genre,
            duration = duration,
            imageUri = savedUri
        )

        _wishlist.add(wish)
        saveData()
    }

    fun removeFilmFromWishlist(wish: Wish) {
        _wishlist.remove(wish)
        saveData()
    }
}

