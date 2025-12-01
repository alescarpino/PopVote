package com.example.popvote.statistics

import com.example.popvote.model.Folder
import com.example.popvote.model.Genre

class StatisticsLogic {


    // most watched genre
    fun getMostWatchedGenre(folders: List<Folder>): Genre? {
        // Collect all film
        val allFilms = folders.flatMap { it.films }
        if (allFilms.isEmpty()) return null
        // Group by genre and count occurrences
        val genreCount = allFilms.groupingBy { it.genre }.eachCount()
        // Return the genre with the highest count
        return genreCount.maxByOrNull { it.value }?.key
    }


    // total watch time in minutes
    fun getTotalMinutesWatched(folders: List<Folder>): Int {
        return folders.sumOf { genre ->
            genre.films.sumOf { it.duration }
        }
    }
}
