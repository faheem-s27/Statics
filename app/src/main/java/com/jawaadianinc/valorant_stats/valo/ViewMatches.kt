package com.jawaadianinc.valorant_stats.valo

import android.content.Intent
import android.os.Bundle
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.net.URL
import java.util.*


class ViewMatches : AppCompatActivity() {
    val playerAgentImage = ArrayList<String>()
    val mapImage = ArrayList<String>()
    val timePlayed = ArrayList<String>()
    val KDA = ArrayList<String>()
    val gameMode = ArrayList<String>()
    val matchIDs = ArrayList<String>()
    val won = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_matches)

        val region = intent.extras?.getString("Region")
        val puuid = intent.extras?.getString("PUUID")
        val numberOfMatches = intent.extras?.getString("NumberOfMatches")

        // this is about to get sticky
        val database = Firebase.database
        val myRef = database.getReference("VALORANT/key")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val key = dataSnapshot.value as String?
                val progessBar: ProgressBar = findViewById(R.id.progressBar6)
                val matchText: TextView = findViewById(R.id.textView10)
                val URL =
                    "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
                doAsync {
                    val response = JSONObject(URL(URL).readText())
                    val url = "https://valorant-api.com/v1/agents"
                    val Agentresponse = JSONObject(URL(url).readText()).getJSONArray("data")
                    val mapURL = "https://valorant-api.com/v1/maps"
                    val mapResponse =
                        JSONObject(URL(mapURL).readText()).getJSONArray("data")
                    val urlName =
                        "https://europe.api.riotgames.com/riot/account/v1/accounts/by-puuid/$puuid?api_key=$key"
                    val responseFromRiot = JSONObject(URL(urlName).readText())
                    val gameNamePlayer = responseFromRiot.getString("gameName")
                    val tagLinePlayer = responseFromRiot.getString("tagLine")

                    val history = response.getJSONArray("history")
                    try {
                        for (i in 0 until history.length()) {
                            if (i >= numberOfMatches!!.toInt()) {
                                break
                            }
                            val currentMatch = history[i] as JSONObject
                            val matchID = currentMatch.getString("matchId")
                            val database = Firebase.database
                            val RSODB = database.getReference("VALORANT/RSO")
                            val URL =
                                "https://$region.api.riotgames.com/val/match/v1/matches/$matchID?api_key=$key"
                            //Log.d("Riot", URL)
                            val response = JSONObject(URL(URL).readText())
                            //Log.d("Riot", response.toString())
                            val matchInfo = response.getJSONObject("matchInfo")
                            val matchStart = matchInfo.getLong("gameStartMillis")
                            val gameDuration = matchInfo.getLong("gameLengthMillis")
                            val map = matchInfo.getString("mapId")
                            var mode = matchInfo.getString("queueId")
                            val players = response.getJSONArray("players")

                            val teams = response.getJSONArray("teams")

                            var agentImage = ""
                            var mapListViewIcon = ""

                            for (i in 0 until players.length()) {
                                val currentPlayer = players[i] as JSONObject
                                val puuid = currentPlayer.getString("puuid")
                                val gameName = currentPlayer.getString("gameName")
                                val gameTag = currentPlayer.getString("tagLine")
                                val playerTeam = currentPlayer.getString("teamId")

                                mode = when (mode) {
                                    "" -> {
                                        "Custom Game"
                                    }
                                    else -> {
                                        mode.replaceFirstChar {
                                            if (it.isLowerCase()) it.titlecase(
                                                Locale.getDefault()
                                            ) else it.toString()
                                        }
                                    }
                                }

                                if (gameName == gameNamePlayer) {
                                    RSODB.child(gameName).child("Puuid").setValue(puuid)
                                    RSODB.child(gameName).child("GameTag").setValue(gameTag)
                                    RSODB.child(gameName).child("Region").setValue(region)
                                    RSODB.child(gameName).child("Matches").child(matchID)
                                        .child("Mode")
                                        .setValue(mode)
                                    val kills = currentPlayer.getJSONObject("stats").getInt("kills")
                                    val deaths =
                                        currentPlayer.getJSONObject("stats").getInt("deaths")
                                    val assists =
                                        currentPlayer.getJSONObject("stats").getInt("assists")
                                    val agentID = currentPlayer.getString("characterId")
                                    for (i in 0 until Agentresponse.length()) {
                                        val currentAgent = Agentresponse[i] as JSONObject
                                        if (currentAgent.getString("uuid") == agentID) {
                                            agentImage = currentAgent.getString("displayIconSmall")
                                            RSODB.child(gameName).child("Matches").child(matchID)
                                                .child("Agent")
                                                .setValue(currentAgent.getString("displayName"))
                                            break
                                        }
                                    }

                                    for (i in 0 until mapResponse.length()) {
                                        val currentMap = mapResponse[i] as JSONObject
                                        if (currentMap.getString("mapUrl") == map) {
                                            mapListViewIcon = currentMap.getString("listViewIcon")
                                            RSODB.child(gameName).child("Matches").child(matchID)
                                                .child("Map")
                                                .setValue(currentMap.getString("displayName"))
                                            break
                                        }
                                    }

                                    val obj = teams[0] as JSONObject

                                    // talking about red team


                                    var winning: String
                                    val redWon = obj.getString("won")

                                    winning = try {
                                        if (redWon == "true" && playerTeam == "Red") {
                                            "true"
                                        } else if (redWon == "false" && playerTeam == "Blue") {
                                            "true"
                                        } else {
                                            "false"
                                        }
                                    } catch (e: Exception) {
                                        "unknown"
                                    }

                                    playerAgentImage += agentImage
                                    mapImage += mapListViewIcon
                                    timePlayed += (matchStart + gameDuration).toString()
                                    KDA += "$kills/$deaths/$assists"
                                    gameMode += mode
                                    matchIDs += matchID
                                    won += winning
                                }
                            }
                            runOnUiThread {
                                matchText.text = "Processed ${i + 1}/$numberOfMatches matches"
                                progessBar.max = numberOfMatches.toInt()
                                progessBar.progress = i + 1
                                val matchList: ListView = findViewById(R.id.matchList)
                                val matches = MatchAdapter(
                                    this@ViewMatches,
                                    playerAgentImage,
                                    mapImage,
                                    timePlayed,
                                    KDA,
                                    gameMode,
                                    matchIDs,
                                    won
                                )
                                matches.notifyDataSetChanged()
                                matchList.adapter = matches
                                matchList.setOnItemClickListener { _, _, position, _ ->
                                    matchActivityStart(
                                        gameNamePlayer,
                                        tagLinePlayer,
                                        matchIDs[position]
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@ViewMatches,
                                "Too many requests!\nPlease wait before requesting again",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                    runOnUiThread {
                        val progessBar: ProgressBar = findViewById(R.id.progressBar6)
                        val matchText: TextView = findViewById(R.id.textView10)
                        val matchList: ListView = findViewById(R.id.matchList)
                        matchList.animate().alpha(1f).duration = 1000
                        progessBar.animate().alpha(0f).duration = 1000
                        matchText.animate().alpha(0f).duration = 1000
                        matchList.setOnItemClickListener { _, _, position, _ ->
                            matchActivityStart(gameNamePlayer, tagLinePlayer, matchIDs[position])
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        // the end of torture

    }

    override fun onResume() {
        super.onResume()
        FirebaseApp.initializeApp(/*context=*/this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }

    private fun matchActivityStart(Name: String, ID: String, matchID: String) {
        val matchintent = Intent(this@ViewMatches, MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", 0)
        matchintent.putExtra("MatchID", matchID)
        startActivity(matchintent)

    }
}
