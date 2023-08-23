package com.jawaadianinc.valorant_stats.valo.activities.new_ui.ValorantLocalisation

data class Act(
    val id: String,
    val isActive: Boolean,
    val localizedNames: LocalizedNames,
    val name: String,
    val parentId: String,
    val type: String
)
