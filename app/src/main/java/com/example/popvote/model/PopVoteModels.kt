package com.example.popvote.model

import android.net.Uri
import java.util.UUID

// data structure classes

data class Folder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val imageUri: Uri? = null, // folder photo
    val films: MutableList<Film> = mutableListOf()
)
data class Film(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val genre: Genre, // Dropdown
    val rating: Int, // from 1 to 5
    val duration: Int,
    val imageUri: Uri? = null // film photo
)
enum class Genre {
    ACTION,
    COMEDY,
    DRAMA,
    HORROR,
    SCI_FI,
    ROMANCE,
    DOCUMENTARY
}
// Container class in which we save all the datas in a JSON file
data class AppData(
    val folders: List<Folder>,
    val allFilms: List<Film>,
    val wishlist: List<Wish>
)

data class Wish(
    val id: String = UUID.randomUUID().toString(),
    val title: String,
    val description: String,
    val genre: Genre, // Dropdown
    val duration: Int,
    val imageUri: Uri? = null // film photo
)