package com.jawaadianinc.valorant_stats.valo.match_info

data class Data(
    val coaches: List<Any>,
    val kills: List<Kill>,
    val metadata: Metadata,
    val observers: List<Any>,
    val players: Players,
    val rounds: List<Round>,
    val teams: Teams
)
