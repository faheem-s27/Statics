package com.jawaadianinc.valorant_stats.valo.activities

import org.json.JSONObject
import java.net.URL

class TrackerGGScraper {
    var profileURL = "https://api.tracker.gg/api/v2/valorant/standard/profile/riot/"

    fun getProfile(name: String, tag: String): JSONObject {
        val url = profileURL + "${name}%23${tag}"
        return JSONObject(URL(url).readText())
    }
}
