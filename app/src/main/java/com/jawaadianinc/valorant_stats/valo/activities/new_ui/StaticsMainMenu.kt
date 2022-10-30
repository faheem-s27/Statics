package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.jawaadianinc.valorant_stats.R
import org.json.JSONObject

class StaticsMainMenu : AppCompatActivity() {
    var playerName: String? = null
    var region: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statics_main_menu)

        // get the player name from the previous activity
        playerName = intent.getStringExtra("playerName")
        region = intent.getStringExtra("region")
        getLastMatch()

    }

    private fun getLastMatch() {
        // split the player name into two parts by # and set it to RiotName and RiotID
        val playerNameSplit = playerName!!.split("#".toRegex()).toTypedArray()
        val riotName = playerNameSplit[0]
        val riotID = playerNameSplit[1]
        val allmatches =
            "https://api.henrikdev.xyz/valorant/v3/matches/$region/$riotName/$riotID?size=1"

        // get the last match
        val queue = Volley.newRequestQueue(this)
        val request = JsonObjectRequest(
            Request.Method.GET, allmatches, null,
            { response ->
                val matchData = response.getJSONArray("matches").getJSONObject(0)
                processMatch(matchData)
            },
            { error ->
                Toast.makeText(this, "Error: $error", Toast.LENGTH_SHORT).show()
            })
        queue.add(request)

    }

    private fun processMatch(matchData: JSONObject) {

    }
}
