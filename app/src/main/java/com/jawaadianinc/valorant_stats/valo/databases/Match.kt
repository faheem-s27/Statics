package com.jawaadianinc.valorant_stats.valo.databases

class Match(timeStarted: String, matchID: String, matchJSON: String) {
    var timeStarted: String = timeStarted
    var matchID: String = matchID
    var matchJSON: String = matchJSON

    override fun toString(): String {
        return "Match(timeStarted='$timeStarted', matchID='$matchID', matchJSON='$matchJSON')"
    }
}
