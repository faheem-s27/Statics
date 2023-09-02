package com.jawaadianinc.valorant_stats.valo.match_info

data class PlantEvents(
    val plant_location: PlantLocation,
    val plant_site: String,
    val plant_time_in_round: Int,
    val planted_by: PlantedBy,
    val player_locations_on_plant: List<PlayerLocationsOnPlant>
)
