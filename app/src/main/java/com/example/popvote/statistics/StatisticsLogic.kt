package com.example.popvote.statistics

import com.example.popvote.model.Folder

class StatisticsLogic {

    // most watched genre
    fun getMostWatchedGenre(folders: List<Folder>): Folder? {
        return folders.maxByOrNull { genre ->
            genre.films.size
        }
    }

    // total watch time in minutes
    fun getTotalMinutesWatched(folders: List<Folder>): Int {
        return folders.sumOf { genre ->
            genre.films.sumOf { it.lengthMinutes }
        }
    }
}
