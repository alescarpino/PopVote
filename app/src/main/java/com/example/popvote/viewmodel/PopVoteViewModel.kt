package com.example.popvote.viewmodel

import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.popvote.model.Film
import com.example.popvote.model.Folder
import com.example.popvote.model.Genre

//this class manages the app's data while it's running
class PopVoteViewModel : ViewModel() {

    // Genres list
    private val _folders = mutableStateListOf<Folder>()
    val folders: List<Folder> get() = _folders

    init {

        _folders.add(Folder(name = "Favourites", films = mutableListOf(
            Film(title = "The Ghost", description = "Very scary movie", genre = Genre.HORROR, rating = 4, lengthMinutes= 110),
            Film(title = "Zombies", description = "They eat brains", genre = Genre.SCI_FI, rating = 3, lengthMinutes= 120)
        )))
        _folders.add(Folder(name = "Fantasy"))
    }

    fun addFolder(name: String, imageUri: Uri?) {
        _folders.add(Folder(name = name, imageUri = imageUri))
    }

    fun deleteFolder(folder: Folder) {
        _folders.remove(folder)
    }

    fun addFilmToFolder(folderId: String, title: String, description: String, genre: Genre, rating: Int, imageUri: Uri?) {
        val folder = _folders.find { it.id == folderId }
        folder?.films?.add(
            Film(title = title, description = description, genre = genre, rating = rating, lengthMinutes = 115, imageUri = imageUri)
        )
    }

    fun deleteFilmFromFolder(folderId: String, film: Film) {
        val folder = _folders.find { it.id == folderId }
        folder?.films?.remove(film)
    }

    // --- ranking---
    // returns all the films of every folder sorted by the valuation
    fun getAllFilmsRanked(): List<Film> {
        return _folders.flatMap { it.films }
            .sortedByDescending { it.rating }
    }

    fun getFolder(id: String): Folder? {
        return _folders.find { it.id == id }
    }
}