package com.jawaadianinc.valorant_stats.valo.match_info

data class Round(
    val bomb_defused: Boolean,
    val bomb_planted: Boolean,
    val defuse_events: DefuseEvents,
    val end_type: String,
    val plant_events: PlantEvents,
    val player_stats: List<PlayerStat>,
    val winning_team: String
)
