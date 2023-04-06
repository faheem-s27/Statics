package com.jawaadianinc.valorant_stats.valo.activities.new_ui

class Leaderboard(
    var name: String, var rank: Int, var playerCardID: String, var mmr: Int, var wins: Int
) {

    override fun toString(): String {
        return "Leaderboard(name='$name', rank=$rank, playerCardID='$playerCardID', mmr=$mmr)"
    }

    fun getSmallImage(): String {
        return "https://media.valorant-api.com/playercards/$playerCardID/smallart.png"
    }

}
