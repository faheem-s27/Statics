package com.jawaadianinc.valorant_stats.valo.match_info

data class DefuseEvents(
    val defuse_location: DefuseLocation,
    val defuse_time_in_round: Int,
    val defused_by: DefusedBy,
    val player_locations_on_defuse: List<PlayerLocationsOnDefuse>
)
