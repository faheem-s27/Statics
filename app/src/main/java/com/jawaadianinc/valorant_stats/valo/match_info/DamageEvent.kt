package com.jawaadianinc.valorant_stats.valo.match_info

data class DamageEvent(
    val bodyshots: Int,
    val damage: Int,
    val headshots: Int,
    val legshots: Int,
    val receiver_display_name: String,
    val receiver_puuid: String,
    val receiver_team: String
)
