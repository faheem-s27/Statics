package com.jawaadianinc.valorant_stats.valorant

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.net.URL


class ViewMatches : AppCompatActivity() {
    val playerAgentImage = ArrayList<String>()
    val mapImage = ArrayList<String>()
    val timePlayed = ArrayList<String>()
    val KDA = ArrayList<String>()
    val gameMode = ArrayList<String>()
    val matchIDs = ArrayList<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_matches)

        // this is about to get sticky
        val database = Firebase.database
        val myRef = database.getReference("VALORANT/key")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val key = dataSnapshot.value as String?
                val progessBar: ProgressBar = findViewById(R.id.progressBar6)
                val matchText: TextView = findViewById(R.id.textView10)
                val matchList: ListView = findViewById(R.id.matchList)
                matchList.alpha = 0f

                val playerName = PlayerDatabase(this@ViewMatches).getPlayerName()
                val split = playerName!!.split("#")
                val puuid = PlayerDatabase(this@ViewMatches).getPUUID(split[0], split[1])
                val region = PlayerDatabase(this@ViewMatches).getRegion(puuid = puuid!!)
                val URL =
                    "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
                doAsync {
                    val response = JSONObject(URL(URL).readText())
                    val history = response.getJSONArray("history")
                    for (i in 0 until history.length()) {
                        val currentMatch = history[i] as JSONObject
                        val matchID = currentMatch.getString("matchId")
                        //processMatchID(matchID, key!!, region!!, split[0], split[1])
                        val database = Firebase.database
                        val RSODB = database.getReference("VALORANT/RSO")
                        val URL =
                            "https://$region.api.riotgames.com/val/match/v1/matches/$matchID?api_key=$key"
                        val response = JSONObject(URL(URL).readText())
                        //Log.d("match", response.toString())
                        val matchInfo = response.getJSONObject("matchInfo")
                        val matchStart = matchInfo.getLong("gameStartMillis")
                        //Log.d("match", "Original: "+ matchStart.toString())
                        val map = matchInfo.getString("mapId")
                        val mode = matchInfo.getString("queueId")
                        val players = response.getJSONArray("players")
                        for (i in 0 until players.length()) {
                            val currentPlayer = players[i] as JSONObject
                            val puuid = currentPlayer.getString("puuid")
                            val gameName = currentPlayer.getString("gameName")
                            val gameTag = currentPlayer.getString("tagLine")
                            RSODB.child(gameName).child("Puuid").setValue(puuid)
                            RSODB.child(gameName).child("GameTag").setValue(gameTag)
                            RSODB.child(gameName).child("Region").setValue(region)
                            if (gameName == split[0]) {
                                val kills = currentPlayer.getJSONObject("stats").getInt("kills")
                                val deaths = currentPlayer.getJSONObject("stats").getInt("deaths")
                                val assists = currentPlayer.getJSONObject("stats").getInt("assists")
                                val agentID = currentPlayer.getString("characterId")
                                var agentImage = ""
                                var mapName = ""
                                //this is to get agentName + mapName
                                val url = "https://valorant-api.com/v1/agents"
                                val response = JSONObject(URL(url).readText()).getJSONArray("data")
                                for (i in 0 until response.length()) {
                                    val currentAgent = response[i] as JSONObject
                                    if (currentAgent.getString("uuid") == agentID) {
                                        agentImage = currentAgent.getString("displayIconSmall")
                                        break
                                    }
                                }
                                val mapURL = "https://valorant-api.com/v1/maps"
                                val mapResponse =
                                    JSONObject(URL(mapURL).readText()).getJSONArray("data")
                                for (i in 0 until mapResponse.length()) {
                                    val currentMap = mapResponse[i] as JSONObject
                                    if (currentMap.getString("mapUrl") == map) {
                                        mapName = currentMap.getString("listViewIcon")
                                        break
                                    }
                                }

                                playerAgentImage += agentImage
                                mapImage += mapName
                                timePlayed += matchStart.toString()
                                KDA += "$kills/$deaths/$assists"
                                gameMode += mode
                                matchIDs += matchID
                            }
                        }
                        runOnUiThread {
                            matchText.text = "Processing ${i + 1}/${history.length()} matches"
                            val number = history.length() / 100.toFloat()
                            val progress = (i + 1) / number
                            progessBar.max = history.length()
                            progessBar.progress = i + 1
                            //Log.d("match", "Progress: $progress")
                            val playerName = PlayerDatabase(this@ViewMatches).getPlayerName()
                            val split = playerName!!.split("#")
                            val matchList: ListView = findViewById(R.id.matchList)
                            matchList.alpha = progress / 100
                            val matches = MatchAdapter(
                                this@ViewMatches,
                                playerAgentImage,
                                mapImage,
                                timePlayed,
                                KDA,
                                gameMode,
                                matchIDs
                            )
                            matchList.adapter = matches
                            matchList.setOnItemClickListener { _, _, position, _ ->
                                val matchID = matchIDs[position]
                                matchActivityStart(split[0], split[1], matchID)
                            }
                        }
                    }
                    runOnUiThread {
                        val progessBar: ProgressBar = findViewById(R.id.progressBar6)
                        val matchText: TextView = findViewById(R.id.textView10)
                        val matchList: ListView = findViewById(R.id.matchList)
                        progessBar.visibility = View.INVISIBLE
                        matchText.visibility = View.INVISIBLE
                        matchList.alpha = 1f
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
        // the end of torture

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
