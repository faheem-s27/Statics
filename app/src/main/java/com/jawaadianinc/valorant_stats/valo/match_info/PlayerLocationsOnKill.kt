package com.jawaadianinc.valorant_stats.valo.match_info

data class PlayerLocationsOnKill(
    val location: Location,
    val player_display_name: String,
    val player_puuid: String,
    val player_team: String,
    val view_radians: Double
)
