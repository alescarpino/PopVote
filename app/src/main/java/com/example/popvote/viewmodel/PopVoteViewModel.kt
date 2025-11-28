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

//this class manages the app's datas while it's running
class PopVoteViewModel(application: Application) : AndroidViewModel(application) {
    //initializing storage manager
    private val storageManager = StorageManager(application)
    // Genres list
    private val _folders = mutableStateListOf<Folder>()
    val folders: List<Folder> get() = _folders

    init {
        //loading of the previous datas
        loadData()
    }
    private fun loadData() {
        val loadedData = storageManager.loadAll()

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
        if (loadedData != null) {
            // if there is a file we'll use it
            _genres.clear()
            _genres.addAll(loadedData.genres)

        } else {
            // default data (beginning)
            _genres.add(Genre(id = UUID.randomUUID().toString(), name = "Horror", films = mutableListOf(
                Film(title = "The Ghost", description = "Very scary movie", rating = 4, lengthMinutes= 110)
            )))
            _genres.add(Genre(id = UUID.randomUUID().toString(), name = "Fantasy"))
        }
    }
    private fun saveData(){
        storageManager.saveAll(_genres.toList(),emptyList())
    }


    fun addGenre(name: String, imageUri: Uri?) {
        val savedUri = imageUri?.let { storageManager.copyImageToInternalStorage(it) }

        _genres.add(Genre(id = UUID.randomUUID().toString(), name = name, imageUri = savedUri))
        saveData()
    }

    fun deleteGenre(genre: Genre) {
        _genres.remove(genre)
        saveData()
    }


    fun addFilmToGenre(genreId: String, title: String, description: String, rating: Int, duration: Int, imageUri: Uri?) {
        val index = _genres.indexOfFirst { it.id == genreId }

        if (index != -1) {
            // saving photo
            val savedUri = imageUri?.let { storageManager.copyImageToInternalStorage(it) }

            // creaitng film
            val newFilm = Film(
                title = title,
                description = description,
                rating = rating,
                lengthMinutes = duration,
                imageUri = savedUri
            )


            val oldGenre = _genres[index]
            oldGenre.films.add(newFilm)

            //  alert for the UI that the list is changed
            _genres[index] = oldGenre.copy()

            saveData()
        }
    }

    fun deleteFilmFromGenre(genreId: String, film: Film) {
        val genre = _genres.find { it.id == genreId }
        genre?.films?.remove(film)
        //updating UI
        val index = _genres.indexOf(genre)
        if(index != -1) _genres[index] = genre!!.copy()

        saveData()
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
