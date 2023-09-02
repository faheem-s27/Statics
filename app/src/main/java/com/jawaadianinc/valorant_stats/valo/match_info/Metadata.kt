package com.jawaadianinc.valorant_stats.valo.match_info

data class Metadata(
    val cluster: String,
    val game_length: Int,
    val game_start: Int,
    val game_start_patched: String,
    val game_version: String,
    val map: String,
    val matchid: String,
    val mode: String,
    val mode_id: String,
    val platform: String,
    val premier_info: PremierInfo,
    val queue: String,
    val region: String,
    val rounds_played: Int,
    val season_id: String
)
