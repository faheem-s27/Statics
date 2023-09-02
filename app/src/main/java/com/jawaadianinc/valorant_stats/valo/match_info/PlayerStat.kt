package com.jawaadianinc.valorant_stats.valo.match_info

data class PlayerStat(
    val ability_casts: AbilityCastsXXX,
    val bodyshots: Int,
    val damage: Int,
    val damage_events: List<DamageEvent>,
    val economy: EconomyXXX,
    val headshots: Int,
    val kill_events: List<KillEvent>,
    val kills: Int,
    val legshots: Int,
    val player_display_name: String,
    val player_puuid: String,
    val player_team: String,
    val score: Int,
    val stayed_in_spawn: Boolean,
    val was_afk: Boolean,
    val was_penalized: Boolean
)
