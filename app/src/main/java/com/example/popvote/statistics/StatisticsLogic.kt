package com.example.popvote.statistics

import com.example.popvote.model.Genre

class StatisticsLogic {

    // most watched genre
    fun getMostWatchedGenre(genres: List<Genre>): Genre? {
        return genres.maxByOrNull { genre ->
            genre.films.size
        }
    }

    // total watch time in minutes
    fun getTotalMinutesWatched(genres: List<Genre>): Int {
        return genres.sumOf { genre ->
            genre.films.sumOf { it.lengthMinutes }
        }
    }
}
