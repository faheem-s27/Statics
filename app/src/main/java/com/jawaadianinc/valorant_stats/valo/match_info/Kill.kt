package com.jawaadianinc.valorant_stats.valo.match_info

data class Kill(
    val assistants: List<Assistant>,
    val damage_weapon_assets: DamageWeaponAssets,
    val damage_weapon_id: String,
    val damage_weapon_name: String,
    val kill_time_in_match: Int,
    val kill_time_in_round: Int,
    val killer_display_name: String,
    val killer_puuid: String,
    val killer_team: String,
    val player_locations_on_kill: List<PlayerLocationsOnKill>,
    val round: Int,
    val secondary_fire_mode: Boolean,
    val victim_death_location: VictimDeathLocation,
    val victim_display_name: String,
    val victim_puuid: String,
    val victim_team: String
)
