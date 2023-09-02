package com.jawaadianinc.valorant_stats.valo.match_info

data class Players(
    val all_players: List<AllPlayer>,
    val blue: List<Blue>,
    val red: List<Red>
)
