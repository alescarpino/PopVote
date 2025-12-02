
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

    private val _wishlist = mutableStateListOf<Film>()
    val wishlist: List<Film> get() = _wishlist

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

    fun addFilmToWishlist(film: Film) {
        _wishlist.add(film)
        saveData()
    }

    fun removeFilmFromWishlist(film: Film) {
        _wishlist.remove(film)
        saveData()
    }
}

