package com.example.popvote.viewmodel

import android.R.attr.duration
import android.net.Uri
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import com.example.popvote.model.Film
import com.example.popvote.model.Genre

//this class manages the app's datas while it's running
class PopVoteViewModel : ViewModel() {

    // Genres list
    private val _genres = mutableStateListOf<Genre>()
    val genres: List<Genre> get() = _genres

    init {

        _genres.add(Genre(name = "Horror", films = mutableListOf(
            Film(title = "The Ghost", description = "Very scary movie", rating = 4, lengthMinutes= 110),
            Film(title = "Zombies", description = "They eat brains", rating = 3, lengthMinutes= 120)
        )))
        _genres.add(Genre(name = "Fantasy"))
    }


    fun addGenre(name: String, imageUri: Uri?) {
        _genres.add(Genre(name = name, imageUri = imageUri))
    }

    fun deleteGenre(genre: Genre) {
        _genres.remove(genre)
    }


    fun addFilmToGenre(genreId: String, title: String, description: String, rating: Int, imageUri: Uri?) {
        val genre = _genres.find { it.id == genreId }
        genre?.films?.add(
            Film(title = title, description = description, rating = rating, lengthMinutes = 115, imageUri = imageUri)
        )
    }

    fun deleteFilmFromGenre(genreId: String, film: Film) {
        val genre = _genres.find { it.id == genreId }
        genre?.films?.remove(film)
    }

    // --- ranking---
    // returns all the films og every genres sorted by the valutation
    fun getAllFilmsRanked(): List<Film> {
        return _genres.flatMap { it.films }
            .sortedByDescending { it.rating }
    }

    fun getGenre(id: String): Genre? {
        return _genres.find { it.id == id }
    }
}