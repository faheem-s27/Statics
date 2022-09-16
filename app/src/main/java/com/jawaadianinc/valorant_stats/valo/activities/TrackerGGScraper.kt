package com.jawaadianinc.valorant_stats.valo.activities

import android.app.Activity
import com.jawaadianinc.valorant_stats.valo.databases.TrackerDB
import org.json.JSONObject
import java.net.URL

class TrackerGGScraper {
    private var profileURL = "https://api.tracker.gg/api/v2/valorant/standard/profile/riot/"
    private var json: JSONObject? = null
    private var name = ""
    private var tag = ""

    private fun buildPlayerURL(name: String, tag: String): String {
        this.name = name
        this.tag = tag
        return "${name}%23${tag}"
    }

    fun getProfile(name: String, tag: String): JSONObject {
        val url = profileURL + buildPlayerURL(name, tag)
        json = JSONObject(URL(url).readText())
        return json as JSONObject
    }

    fun getMaps(mode: String) {
        val url = profileURL + buildPlayerURL(name, tag) + "/segments/map?playlist=${mode}"
        mapJSON = JSONObject(URL(url).readText())
    }

    fun getWeapons(mode: String) {
        val url = profileURL + buildPlayerURL(name, tag) + "/segments/weapon?playlist=${mode}"
        weaponJSON = JSONObject(URL(url).readText())
    }

    fun getAgents(mode: String) {
        val url = profileURL + buildPlayerURL(name, tag) + "/segments/agent?playlist=${mode}"
        agentJSON = JSONObject(URL(url).readText())

    }

    fun putToDatabase(mode: String, context: Activity, playerName: String) {
        val agentJSON = agentJSON.toString().replace("'", "")
        val mapJSON = mapJSON.toString().replace("'", "")
        val weaponJSON = weaponJSON.toString().replace("'", "")

        val db = TrackerDB(context)
        if (db.checkIfDataExists(mode, playerName)) {
            db.updateDetails(playerName, agentJSON, weaponJSON, mapJSON, mode)
        } else {
            db.insertDetails(
                playerName,
                agentJSON.toString(),
                weaponJSON.toString(),
                mapJSON.toString(),
                mode
            )
        }
    }

    fun getMapJSON(): JSONObject {
        // omit all of the single quotes in the JSON
        val mapJSONString = mapJSON.toString().replace("'", "")
        return JSONObject(mapJSONString)
    }

    fun getWeaponJSON(): JSONObject {
        // omit all of the single quotes in the JSON
        val weaponJSONString = weaponJSON.toString().replace("'", "")
        return JSONObject(weaponJSONString)
    }

    fun getAgentJSON(): JSONObject {
        // omit all of the single quotes in the JSON
        val agentJSONString = agentJSON.toString().replace("'", "")
        return JSONObject(agentJSONString)
    }

    companion object {
        var mapJSON: JSONObject? = null
        var agentJSON: JSONObject? = null
        var weaponJSON: JSONObject? = null
    }


}
