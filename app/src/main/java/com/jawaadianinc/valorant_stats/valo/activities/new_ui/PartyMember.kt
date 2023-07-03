package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.Context
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase

class PartyMember(
    val gameName: String,
    val gameTag: String,
    val titleID: String,
    val cardID: String,
    val isReady: Boolean,
    val region: String,
    val rankImage: String
) {
    fun getPlayerImage(): String {
        return "https://media.valorant-api.com/playercards/${cardID}/smallart.png"
    }

    fun getTitle(context: Context): String {
        return AssetsDatabase(context).retrieveName(titleID)
    }
}
