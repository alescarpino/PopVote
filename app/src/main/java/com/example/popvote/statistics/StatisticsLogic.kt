package com.example.popvote.statistics

import com.example.popvote.model.Folder
import com.example.popvote.model.Genre
import com.example.popvote.model.Film
class StatisticsLogic {


    // most watched genre
    fun getMostWatchedGenre(films: List<Film>): Genre? {

        if (films.isEmpty()) return null
        // Group by genre and count occurrences
        val genreCount = films.groupingBy { it.genre }.eachCount()
        // Return the genre with the highest count
        return genreCount.maxByOrNull { it.value }?.key
    }


    // total watch time in minutes
    fun getTotalMinutesWatched(films: List<Film>): Int {
        return films.sumOf { it.duration }
    }
}
