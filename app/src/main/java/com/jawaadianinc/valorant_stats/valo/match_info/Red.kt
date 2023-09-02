package com.jawaadianinc.valorant_stats.valo.match_info

data class Red(
    val ability_casts: AbilityCasts,
    val assets: Assets,
    val behavior: Behavior,
    val character: String,
    val currenttier: Int,
    val currenttier_patched: String,
    val damage_made: Int,
    val damage_received: Int,
    val economy: EconomyXX,
    val level: Int,
    val name: String,
    val party_id: String,
    val platform: PlatformXX,
    val player_card: String,
    val player_title: String,
    val puuid: String,
    val session_playtime: SessionPlaytime,
    val stats: Stats,
    val tag: String,
    val team: String
)
