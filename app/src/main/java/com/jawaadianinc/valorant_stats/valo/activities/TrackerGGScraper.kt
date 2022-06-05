package com.jawaadianinc.valorant_stats.valo.activities

import org.json.JSONObject
import java.net.URL

class TrackerGGScraper {
    var profileURL = "https://api.tracker.gg/api/v2/valorant/standard/profile/riot/"

    private fun buildPlayerURL(name: String, tag: String): String {
        return "${name}%23${tag}"
    }

    fun getProfile(name: String, tag: String): JSONObject {
        val url = profileURL + buildPlayerURL(name, tag)
        return JSONObject(URL(url).readText())
    }
}
