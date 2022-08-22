package com.jawaadianinc.valorant_stats.valo.classes

class Player(
    val name: String,
    val team: String,
    val level: Int,
    val kills: Int,
    val deaths: Int,
    val assists: Int,
    val score: Int,
    val agent: Agent,
    val rank: Rank
) {
    // get player name and tag from name by delimeter #
    fun getNameAndTag(): Pair<String, String> {
        val nameAndTag = name.split("#")
        return Pair(nameAndTag[0], nameAndTag[1])
    }
}

class Agent(val name: String, val url: String)

class Rank(val name: String, val url: String)
