package com.jawaadianinc.valorant_stats.valo.activities

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
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
import com.jawaadianinc.valorant_stats.valo.adapters.MatchAdapter
import com.jawaadianinc.valorant_stats.valo.classes.Match
import com.jawaadianinc.valorant_stats.valo.databases.StoredMatchesDatabase
import com.jawaadianinc.valorant_stats.valo.match_info.MatchHistoryActivity
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.net.URL
import java.util.*


class ViewMatches : AppCompatActivity() {
    val playerAgentImage = ArrayList<String>()
    val mapImage = ArrayList<String>()
    val timePlayed = ArrayList<String>()
    val kda = ArrayList<String>()
    val gameMode = ArrayList<String>()
    val matchIDs = ArrayList<String>()
    val won = ArrayList<Boolean>()
    val mapNames = ArrayList<String>()

    private val matchLists = ArrayList<Match>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_matches)

        val region = intent.extras?.getString("Region")
        val puuid = intent.extras?.getString("PUUID")
        val numberOfMatches = intent.extras?.getString("NumberOfMatches")

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar6)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "Match History"
        toolbar.setNavigationOnClickListener {
            finish()
        }

        val rg = findViewById<RadioGroup>(R.id.radioGroup)
        // invisible until everything is loaded
        rg.visibility = View.INVISIBLE

        val matches = MatchAdapter(
            this,
            matchLists
        )

        val matchList: ListView = findViewById(R.id.matchList)
        matchList.adapter = matches
        val progessBar: ProgressBar = findViewById(R.id.progressBar6)
        val matchText: TextView = findViewById(R.id.textView10)
        progessBar.max = numberOfMatches!!.toInt()

        val matchesDB = StoredMatchesDatabase(this)

        // this is about to get sticky
        val database = Firebase.database
        val myRef = database.getReference("VALORANT/key")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val key = dataSnapshot.value as String?

                val url =
                    "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
                doAsync {
                    val response = JSONObject(URL(url).readText())
                    val agentURL = "https://valorant-api.com/v1/agents"
                    val agentResponse = JSONObject(URL(agentURL).readText()).getJSONArray("data")
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
                            if (i >= numberOfMatches.toInt()) {
                                break
                            }
                            val currentMatch = history[i] as JSONObject
                            val matchID = currentMatch.getString("matchId")
                            val timeStarted = currentMatch.getString("gameStartTimeMillis")
                            val matchURL =
                                "https://$region.api.riotgames.com/val/match/v1/matches/$matchID?api_key=$key"

                            var responseRiot: JSONObject
                            if (matchesDB.isinDatabase(timeStarted, matchID)) {
                                responseRiot = JSONObject(matchesDB.getJSON(timeStarted, matchID)!!)
                            } else {
                                responseRiot = JSONObject(URL(matchURL).readText())
                                if (!matchesDB.addMatch(
                                        timeStarted,
                                        matchID,
                                        responseRiot.toString()
                                    )
                                ) {
                                    Log.d("MatchLoadingError", "Failed to add match to database")
                                }
                            }

                            val firebaseDatabase = Firebase.database
                            val rsoDB = firebaseDatabase.getReference("VALORANT/RSO")
                            val matchInfo = responseRiot.getJSONObject("matchInfo")
                            val matchStart = matchInfo.getLong("gameStartMillis")
                            val gameDuration = matchInfo.getLong("gameLengthMillis")
                            val map = matchInfo.getString("mapId")
                            var mode = matchInfo.getString("queueId")
                            val players = responseRiot.getJSONArray("players")
                            val teams = responseRiot.getJSONArray("teams")

                            var agentImage = ""
                            var mapListViewIcon = ""
                            var mapName = ""
                            var kills = 0
                            var deaths = 0
                            var assists = 0
                            var winning: Boolean = false

                            for (j in 0 until players.length()) {
                                val currentPlayer = players[j] as JSONObject
                                val playerPUUID = currentPlayer.getString("puuid")
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
                                    rsoDB.child(gameName).child("Puuid").setValue(playerPUUID)
                                    rsoDB.child(gameName).child("GameTag").setValue(gameTag)
                                    rsoDB.child(gameName).child("Region").setValue(region)
                                    rsoDB.child(gameName).child("Matches").child(matchID)
                                        .child("Mode")
                                        .setValue(mode)
                                    kills = currentPlayer.getJSONObject("stats").getInt("kills")
                                    deaths =
                                        currentPlayer.getJSONObject("stats").getInt("deaths")
                                    assists =
                                        currentPlayer.getJSONObject("stats").getInt("assists")
                                    val agentID = currentPlayer.getString("characterId")
                                    for (k in 0 until agentResponse.length()) {
                                        val currentAgent = agentResponse[k] as JSONObject
                                        if (currentAgent.getString("uuid") == agentID) {
                                            agentImage = currentAgent.getString("displayIconSmall")
                                            rsoDB.child(gameName).child("Matches").child(matchID)
                                                .child("Agent")
                                                .setValue(currentAgent.getString("displayName"))
                                            break
                                        }
                                    }

                                    for (k in 0 until mapResponse.length()) {
                                        val currentMap = mapResponse[k] as JSONObject
                                        if (currentMap.getString("mapUrl") == map) {
                                            mapListViewIcon = currentMap.getString("listViewIcon")
                                            mapName = currentMap.getString("displayName")
                                            rsoDB.child(gameName).child("Matches").child(matchID)
                                                .child("Map")
                                                .setValue(currentMap.getString("displayName"))
                                            break
                                        }
                                    }

                                    val obj = teams[0] as JSONObject

                                    // talking about red team
                                    val redWon = obj.getString("won")

                                    winning = try {
                                        if (redWon == "true" && playerTeam == "Red") {
                                            true
                                        } else redWon == "false" && playerTeam == "Blue"
                                    } catch (e: Exception) {
                                        false
                                    }
                                }
                            }

                            runOnUiThread {
                                matchText.text = "Processed ${i + 1}/$numberOfMatches matches"
                                progessBar.progress = i + 1

                                playerAgentImage += agentImage
                                mapImage += mapListViewIcon
                                timePlayed += (matchStart + gameDuration).toString()
                                kda += "$kills/$deaths/$assists"
                                gameMode += mode
                                matchIDs += matchID
                                won += winning
                                mapNames += mapName

                                val match = Match(
                                    (matchStart + gameDuration).toString(),
                                    mode,
                                    kills,
                                    deaths,
                                    assists,
                                    mapListViewIcon,
                                    agentImage,
                                    winning,
                                    matchID,
                                )

                                matchLists.add(match)
                                matches.notifyDataSetChanged()
                                matchList.setOnItemClickListener { _, _, position, _ ->
                                    matchActivityStart(
                                        gameNamePlayer,
                                        tagLinePlayer,
                                        matchIDs[position],
                                        won[position]
                                    )
                                }
                            }
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(
                                this@ViewMatches,
                                "Request limit has been reached, please try again in a few minutes",
                                Toast.LENGTH_LONG
                            ).show()

                            // Log the error
                            Log.e("MatchLoadingError", e.toString())
                        }
                    }
                    runOnUiThread {
                        val progressBar: ProgressBar = findViewById(R.id.progressBar6)
                        val matchText1: TextView = findViewById(R.id.textView10)

                        // get shared preferences to check if the user has cached matches before
                        val sharedPref = getSharedPreferences("cache", Context.MODE_PRIVATE)
                        val cached = sharedPref.getBoolean("cached", false)

                        // if the user hasn't cached matches before, show the toast message
                        if (!cached) {
                            Toast.makeText(
                                this@ViewMatches,
                                "Your matches have been saved! They will load faster next time",
                                Toast.LENGTH_LONG
                            ).show()
                            // set the shared preference to true so that the user doesn't get the toast message again
                            with(sharedPref.edit()) {
                                putBoolean("cached", true)
                                apply()
                            }
                        }

                        // check if matchesDB has exceeded 100MB
                        var size = matchesDB.numberOfMatches()
                        if (size > 100) {
                            // show alert dialog
                            // get shared preferences to check if the user has been alerted before
                            val sharedPref = getSharedPreferences("cache", Context.MODE_PRIVATE)
                            val alerted = sharedPref.getBoolean("alerted", false)
                            if (!alerted) {
                                val builder = AlertDialog.Builder(this@ViewMatches)
                                builder.setTitle("Matches Database Size")
                                builder.setMessage("Your matches cache has exceeded 100 matches.\n\nStatics will delete the oldest matches to save space!")
                                builder.setPositiveButton("OK") { _, _ ->
                                    // if it has, delete the database until it goes to 40MB
                                    // set the shared preference to true so that the user doesn't get the alert message again
                                    with(sharedPref.edit()) {
                                        putBoolean("alerted", true)
                                        apply()
                                    }

                                    while (size > 90) {
                                        matchesDB.deleteOldestMatch()
                                        size = matchesDB.numberOfMatches()
                                    }
                                    // show the toast message
                                    Toast.makeText(
                                        this@ViewMatches,
                                        "Storage has been freed up! Your newest matches are still saved",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                builder.setNegativeButton("No") { _, _ -> }
                                builder.show()
                            } else {
                                while (size > 90) {
                                    matchesDB.deleteOldestMatch()
                                    size = matchesDB.numberOfMatches()
                                }
                            }
                        }
                        matchList.animate().alpha(1f).duration = 1000
                        progressBar.animate().alpha(0f).duration = 1000
                        matchText1.animate().alpha(0f).duration = 1000

                        // store the current matchList in another list
                        val matchListAll = matchLists

                        rg.visibility = View.VISIBLE
                        // check the first radio button
                        rg.check(R.id.latestRadio)

                        rg.setOnCheckedChangeListener { _, checkedId ->
                            when (checkedId) {
                                R.id.latestRadio -> {
                                    val adapter = MatchAdapter(this@ViewMatches, matchListAll)
                                    matchList.adapter = adapter
                                    // sort by newest time started
                                    matchListAll.sortByDescending { it.timeStarted.toLong() }
                                    matches.notifyDataSetChanged()
                                    matchList.setOnItemClickListener { _, _, position, _ ->
                                        matchActivityStart(
                                            gameNamePlayer,
                                            tagLinePlayer,
                                            matchListAll[position].matchID,
                                            matchListAll[position].won
                                        )
                                    }
                                }
                                R.id.oldestRadio -> {
                                    val adapter = MatchAdapter(this@ViewMatches, matchListAll)
                                    matchList.adapter = adapter
                                    // sort by oldest time started
                                    matchListAll.sortBy { it.timeStarted.toLong() }
                                    matches.notifyDataSetChanged()
                                    matchList.setOnItemClickListener { _, _, position, _ ->
                                        matchActivityStart(
                                            gameNamePlayer,
                                            tagLinePlayer,
                                            matchListAll[position].matchID,
                                            matchListAll[position].won
                                        )
                                    }
                                }
                                R.id.compRadio -> {
                                    // only show competitive matches
                                    val compAdapter = MatchAdapter(this@ViewMatches,
                                        matchListAll.filter { it.gameMode == "Competitive" } as ArrayList<Match>)
                                    val compList =
                                        matchListAll.filter { it.gameMode == "Competitive" } as ArrayList<Match>
                                    matchList.adapter = compAdapter
                                    matches.notifyDataSetChanged()
                                    matchList.setOnItemClickListener { _, _, position, _ ->
                                        matchActivityStart(
                                            gameNamePlayer,
                                            tagLinePlayer,
                                            compList[position].matchID,
                                            compList[position].won
                                        )
                                    }
                                }
                                R.id.unratedRadio -> {
                                    // only show unrated matches
                                    val unratedAdapter = MatchAdapter(this@ViewMatches,
                                        matchListAll.filter { it.gameMode == "Unrated" } as ArrayList<Match>)
                                    val matchListUnrated =
                                        matchListAll.filter { it.gameMode == "Unrated" } as ArrayList<Match>
                                    matchList.adapter = unratedAdapter
                                    matches.notifyDataSetChanged()
                                    matchList.setOnItemClickListener { _, _, position, _ ->
                                        matchActivityStart(
                                            gameNamePlayer,
                                            tagLinePlayer,
                                            matchListUnrated[position].matchID,
                                            matchListUnrated[position].won
                                        )
                                    }
                                }
                                R.id.spikeRushRadio -> {
                                    // only show spike rush matches
                                    val spikeRushAdapter = MatchAdapter(this@ViewMatches,
                                        matchListAll.filter { it.gameMode == "Spikerush" } as ArrayList<Match>)
                                    val matchListSpikeRush =
                                        matchListAll.filter { it.gameMode == "Spikerush" } as ArrayList<Match>
                                    matchList.adapter = spikeRushAdapter
                                    matches.notifyDataSetChanged()
                                    matchList.setOnItemClickListener { _, _, position, _ ->
                                        matchActivityStart(
                                            gameNamePlayer,
                                            tagLinePlayer,
                                            matchListSpikeRush[position].matchID,
                                            matchListSpikeRush[position].won
                                        )
                                    }
                                }
                                R.id.killsRadio -> {
                                    val adapter = MatchAdapter(this@ViewMatches, matchListAll)
                                    matchList.adapter = adapter
                                    // sort by most kills
                                    matchListAll.sortByDescending { it.kills }
                                    matches.notifyDataSetChanged()
                                    matchList.setOnItemClickListener { _, _, position, _ ->
                                        matchActivityStart(
                                            gameNamePlayer,
                                            tagLinePlayer,
                                            matchListAll[position].matchID,
                                            matchListAll[position].won
                                        )
                                    }
                                }
                                R.id.deathsRadio -> {
                                    val adapter = MatchAdapter(this@ViewMatches, matchListAll)
                                    matchList.adapter = adapter
                                    // show the highest deaths first
                                    matchLists.sortByDescending { it.deaths }
                                    // show all the matches
                                    matches.notifyDataSetChanged()
                                    matchList.setOnItemClickListener { _, _, position, _ ->
                                        matchActivityStart(
                                            gameNamePlayer,
                                            tagLinePlayer,
                                            matchListAll[position].matchID,
                                            matchListAll[position].won
                                        )
                                    }
                                }
                                R.id.assistsRadio -> {
                                    val adapter = MatchAdapter(this@ViewMatches, matchListAll)
                                    matchList.adapter = adapter
                                    // show the highest assists first
                                    matchLists.sortByDescending { it.assists }
                                    // show all the matches
                                    matches.notifyDataSetChanged()
                                    matchList.setOnItemClickListener { _, _, position, _ ->
                                        matchActivityStart(
                                            gameNamePlayer,
                                            tagLinePlayer,
                                            matchListAll[position].matchID,
                                            matchListAll[position].won
                                        )
                                    }
                                }
                            }
                        }

                        matchList.setOnItemClickListener { _, _, position, _ ->
                            matchActivityStart(
                                gameNamePlayer,
                                tagLinePlayer,
                                matchIDs[position],
                                won[position]
                            )
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

    private fun matchActivityStart(Name: String, ID: String, matchID: String, winning: Boolean) {
        val matchintent = Intent(this@ViewMatches, MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", 0)
        matchintent.putExtra("MatchID", matchID)
        matchintent.putExtra("Won", winning)
        startActivity(matchintent)
    }
}
