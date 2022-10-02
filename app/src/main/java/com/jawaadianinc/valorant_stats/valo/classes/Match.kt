package com.jawaadianinc.valorant_stats.valo.classes

class Match(
    val timeStarted: String,
    val gameMode: String,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val mapImage: String,
    val agentImage: String,
    val won: Boolean,
    val matchID: String,
) {
    fun getKDA(): String {
        return "$kills/$deaths/$assists"
    }
}
