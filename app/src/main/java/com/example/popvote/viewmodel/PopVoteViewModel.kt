
package com.example.popvote.viewmodel

import android.app.Application
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import com.example.popvote.data.StorageManager
import com.example.popvote.model.Film
import com.example.popvote.model.Folder
import com.example.popvote.model.Genre
import java.util.UUID

class PopVoteViewModel(application: Application) : AndroidViewModel(application) {

    private val storageManager = StorageManager(application)
    private val _folders = mutableStateListOf<Folder>()
    val folders: List<Folder> get() = _folders

    private val _allFilms = mutableStateListOf<Film>()
    val allFilms: List<Film> get() = _allFilms

    init {
        loadData()
    }

    private fun loadData() {
        _folders.clear()
        _folders.add(
            Folder(
                name = "Favourites",
                films = mutableListOf(
                    Film(
                        title = "The Ghost",
                        description = "Very scary movie",
                        genre = Genre.HORROR,
                        rating = 4,
                        duration = 110
                    ),
                    Film(
                        title = "Zombies",
                        description = "They eat brains",
                        genre = Genre.SCI_FI,
                        rating = 3,
                        duration = 120
                    )
                )
            )
        )
        _folders.add(Folder(name = "Fantasy"))
    }

    private fun saveData() {
        storageManager.saveAll(_folders.toList(), emptyList())
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

    fun getAllFilmsRanked(): List<Film> {
        return _folders.flatMap { it.films }
            .sortedByDescending { it.rating }
    }

    fun getAllFilmsAlphabetical(): List<Film> {
        return _folders.flatMap { it.films }
            .sortedBy { it.title.lowercase() }
    }

    fun getFolder(id: String): Folder? {
        return _folders.find { it.id == id }
    }


    fun addFilm(
        title: String,
        description: String,
        genre: Genre,
        rating: Int,
        duration: Int,
        imageUri: Uri?
    ) {
        val savedUri = imageUri?.let { storageManager.copyImageToInternalStorage(it) }
        val newFilm = Film(
            title = title,
            description = description,
            genre = genre,
            rating = rating,
            duration = duration,
            imageUri = savedUri
        )

        _allFilms.add(newFilm)
        saveData()
    }


}

