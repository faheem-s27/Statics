package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import org.json.JSONObject

data class NightWeaponSkinOffer(
    var displayName: String,
    var displayIcon: String,
    var cost: Int,
    var discountCost: Int,
    var rarity: JSONObject
)
