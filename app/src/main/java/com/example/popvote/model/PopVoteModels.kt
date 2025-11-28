package com.example.popvote.model

import android.net.Uri
import java.util.UUID

// data structure classes

data class Genre(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val imageUri: Uri? = null, // genre photo
    val films: MutableList<Film> = mutableListOf()
)

data class Film(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val rating: Int, // from 1 to 5
    val lengthMinutes: Int,
    val imageUri: Uri? = null // film photo
)
// Container class in which we save all the datas in a JSON file
data class AppData(
    val genres: List<Genre>,
    val wishlist: List<Film>
)