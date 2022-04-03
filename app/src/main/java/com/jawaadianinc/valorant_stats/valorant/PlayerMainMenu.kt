package com.jawaadianinc.valorant_stats.valorant

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.GamePickerMenu
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*

class PlayerMainMenu : AppCompatActivity() {
    private lateinit var imagebackground: ImageView
    private val imagesURL = java.util.ArrayList<String>()
    private lateinit var playerName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaccount)
        val name = PlayerDatabase(this).getPlayerName()
        if (name == null) {
            startActivity(Intent(this, LoggingInActivityRSO::class.java))
            Toast.makeText(this, "Sign in to continue!", Toast.LENGTH_SHORT).show()
        } else {
            playerName = name
        }
        val nameSplit = playerName.split("#")

        imagesURL.add("https://media.valorant-api.com/playercards/3432dc3d-47da-4675-67ae-53adb1fdad5e/largeart.png")
        doAsync {
            val getValoImagesURL =
                JSONObject(URL("https://valorant-api.com/v1/playercards").readText())
            val images = getValoImagesURL["data"] as JSONArray
            for (i in 0 until images.length()) {
                val imageURL = images[i] as JSONObject
                imagesURL.add(imageURL["largeArt"].toString())
            }
            try {
                val data =
                    JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${nameSplit[0]}/${nameSplit[1]}?force=true").readText())["data"] as JSONObject
                val largePic = data.getJSONObject("card").getString("large") as String
                val smolPic = data.getJSONObject("card").getString("small") as String
                val playerProfile: ImageView = findViewById(R.id.playerProfile)
                val playerLevel: TextView = findViewById(R.id.playerLevel)
                uiThread {
                    Picasso.get().load(smolPic).fit().centerInside().into(playerProfile)
                    Picasso.get().load(largePic).fit().centerInside().into(imagebackground)
                    playerLevel.text = data.getInt("account_level").toString()
                }
            } catch (e: Exception) {
                Log.d("Pic", "Error: $e")
            }
        }

        val MMR: FloatingActionButton = findViewById(R.id.MMRFAB)
        val updatesButton: Button = findViewById(R.id.updateBT)
        val leaderBoard: Button = findViewById(R.id.leaderboard)
        val RSOLogOut: Button = findViewById(R.id.RSOLogOut)
        val RecentMatchFAB: FloatingActionButton = findViewById(R.id.RecentMatchFAB)



        imagebackground = findViewById(R.id.imagebackground)
        Picasso.get().load(imagesURL.random()).into(imagebackground)
        //syncFireBase()
        val playerNameText: TextView = findViewById(R.id.playerNameMenu)
        playerNameText.text = playerName


        RSOLogOut.setOnClickListener {
            val playerDB = PlayerDatabase(this)
            if (playerDB.logOutPlayer(nameSplit[0])) {
                startActivity(Intent(this, GamePickerMenu::class.java))
                Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error logging out :(", Toast.LENGTH_SHORT).show()
            }
        }

        RecentMatchFAB.setOnClickListener {
            startActivity(Intent(this, ViewMatches::class.java))
            //findMatches(playerName)
        }


        updatesButton.setOnClickListener {
            startActivity(Intent(this, ValorantUpdatesActivity::class.java))
        }

        MMR.setOnClickListener {
            val fullname = name
            val name = fullname?.split("#")
            val intent = Intent(this@PlayerMainMenu, MMRActivity::class.java)
            intent.putExtra("RiotName", name?.get(0))
            intent.putExtra("RiotID", name?.get(1))
            startActivity(intent)
        }

        leaderBoard.setOnClickListener {
            startActivity(Intent(this, leaderBoardActivity::class.java))
        }

        getRank(nameSplit[0], nameSplit[1])
        getLastMatch(nameSplit[0], nameSplit[1])
        //getKey()

    }

    private fun getRank(RiotName: String, RiotID: String) {
        val rankImageMainMenu: ImageView = findViewById(R.id.rankImageMainMenu)
        val rankPatchedMainMenu: TextView = findViewById(R.id.rankPatchedMainMenu)
        val rankProgressMainMenu: ProgressBar = findViewById(R.id.rankProgressMainMenu)
        val rankNumberMainMenu: TextView = findViewById(R.id.rankNumberMainMenu)

        val tierURL = "https://valorant-api.com/v1/competitivetiers"
        val currentTier = "https://api.henrikdev.xyz/valorant/v1/mmr/eu/${RiotName}/$RiotID"

        doAsync {
            val currentTierData = JSONObject(URL(currentTier).readText())
            val dataofThis = currentTierData["data"] as JSONObject
            val currentTierNumber = dataofThis["currenttier"] as Int
            val progressNumber = dataofThis["ranking_in_tier"] as Int
            val patched = dataofThis["currenttierpatched"] as String
            val tiers = JSONObject(URL(tierURL).readText())
            val tierArray = tiers["data"] as JSONArray
            val tierData = tierArray[3] as JSONObject
            val tiersagain = tierData["tiers"] as JSONArray
            for (j in 0 until tiersagain.length()) {
                val actualTier = tiersagain[j] as JSONObject
                val done = actualTier["tier"] as Int
                if (done == currentTierNumber) {
                    val tierIcon = actualTier["largeIcon"] as String
                    runOnUiThread {
                        rankImageMainMenu.visibility = View.VISIBLE
                        Picasso
                            .get()
                            .load(tierIcon)
                            .fit()
                            .centerInside()
                            .into(rankImageMainMenu)
                        rankImageMainMenu.scaleType = ImageView.ScaleType.FIT_XY
                        rankPatchedMainMenu.text = patched
                        rankProgressMainMenu.progress = progressNumber
                        rankProgressMainMenu.visibility = View.VISIBLE
                        rankNumberMainMenu.text = "$progressNumber/100"
                        rankNumberMainMenu.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // get last match stats given RiotName and RiotID
    private fun getLastMatch(RiotName: String, RiotID: String) {
        val lastMatchMapImage: ImageView = findViewById(R.id.lastMatchMapImage)
        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/eu/$RiotName/$RiotID?size=1"
        val agentImageMainMenu: ImageView = findViewById(R.id.agentImageMainMenu)
        doAsync {
            val lastMatchData = JSONObject(URL(allmatches).readText())
            val jsonOfMap = JSONObject(URL("https://valorant-api.com/v1/maps").readText())
            val mapData = jsonOfMap["data"] as JSONArray
            val data = lastMatchData["data"] as JSONArray
            val metadata = data.getJSONObject(0).getJSONObject("metadata")
            val map = metadata.getString("map")
            var actualtMapUlr = ""

            val unixTimeStart = metadata.getInt("game_start")
            val date = Date(unixTimeStart * 1000L)
            val d: Duration =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Duration.between(
                        date.toInstant(),
                        Instant.now()
                    )
                } else {
                    TODO("VERSION.SDK_INT < O")
                }

            var KDA: String? = null

            val gameModeMainMenu: TextView = findViewById(R.id.gameModeMainMenu)
            val gameMode = metadata.getString("mode")

            val timeinDays = d.toDays()
            val timeInHours = d.toHours()
            val gameStartMainMenu: TextView = findViewById(R.id.gameStartMainMenu)

            var agentURL: String? = null
            val playersList = data.getJSONObject(0).getJSONObject("players")
            val allPlayers = playersList.getJSONArray("all_players") as JSONArray
            for (i in 0 until allPlayers.length()) {
                val currentPlayer = allPlayers[i] as JSONObject
                val currentPlayerName = currentPlayer.getString("name")
                if (currentPlayerName == RiotName) {
                    agentURL = currentPlayer.getJSONObject("assets").getJSONObject("agent")
                        .getString("small")
                    val stats = currentPlayer.getJSONObject("stats")
                    KDA =
                        stats.getString("kills") + "/" + stats.getString("deaths") + "/" + stats.getString(
                            "assists"
                        )
                    break
                }
            }
            val lastMatchStatsMainMenu: TextView = findViewById(R.id.lastMatchStatsMainMenu)
            for (i in 0 until mapData.length()) {
                val mapNamefromJSON = mapData[i] as JSONObject
                val nameofMpa = mapNamefromJSON["displayName"]
                if (nameofMpa == map) {
                    actualtMapUlr = mapNamefromJSON["listViewIcon"].toString()
                }
            }
            runOnUiThread {
                when {
                    timeinDays > 0 -> {
                        gameStartMainMenu.text = "$timeinDays days ago"
                    }
                    timeInHours > 0 -> {
                        gameStartMainMenu.text = "$timeInHours hours ago"
                    }
                    else -> {
                        gameStartMainMenu.text = "${d.toMinutes()} minutes ago"
                    }
                }
                lastMatchStatsMainMenu.text = KDA
                Picasso.get().load(agentURL).fit().centerInside().into(agentImageMainMenu)

                gameModeMainMenu.text = gameMode

                lastMatchMapImage.visibility = View.VISIBLE
                agentImageMainMenu.visibility = View.VISIBLE
                Picasso
                    .get()
                    .load(actualtMapUlr)
                    .fit()
                    .centerInside()
                    .into(lastMatchMapImage)
                lastMatchMapImage.scaleType = ImageView.ScaleType.FIT_XY
                agentImageMainMenu.scaleType = ImageView.ScaleType.FIT_XY
            }
        }
    }

    private fun getKey() {
        val database = Firebase.database
        val myRef = database.getReference("VALORANT/key")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val key = dataSnapshot.value as String?
                getRecentMatches(key!!)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }


    private fun getRecentMatches(key: String) {
        val FullName = playerName.split("#")
        val puuid = PlayerDatabase(this).getPUUID(FullName[0], FullName[1])
        // try to get region from the database and if there is no region, set the default value to "eu"
        val region = if (PlayerDatabase(this).getRegion(puuid = puuid!!) != null) {
            PlayerDatabase(this).getRegion(puuid = puuid)
        } else {
            "eu"
        }

        val URL =
            "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
        doAsync {
            val response = JSONObject(URL(URL).readText())
            val history = response.getJSONArray("history")
            for (i in 0 until history.length()) {
                val currentMatch = history[i] as JSONObject
                val matchID = currentMatch.getString("matchId")
                Log.d("matchID", matchID)
            }
            Log.d("matchID", "Found ${history.length()} matches")
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    private fun matchActivityStart(Name: String, ID: String, matchNumber: Int, matchID: String) {
        val matchintent = Intent(this@PlayerMainMenu, MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", matchNumber)
        matchintent.putExtra("MatchID", matchID)
        startActivity(matchintent)
    }

    private fun syncFireBase() {
        val matchDatabse = MatchDatabases(this@PlayerMainMenu)
        try {
            val userList = java.util.ArrayList<String>()
            val sqlString =
                "SELECT ${MatchDatabases.USER} FROM ${MatchDatabases.USERMATCHES}"
            val db = matchDatabse.readableDatabase
            val cursor = db.rawQuery(sqlString, null)
            if (cursor.moveToFirst()) do {
                val users = cursor.getString(0)
                if (!userList.contains(users)) {
                    userList.add(users)
                }
            } while (cursor.moveToNext())
            val database = Firebase.database
            val playersRef = database.getReference("VALORANT/players")
            cursor.close()
            db.close()

            for (user in userList) {
                val userFinal = user.split("#")
                val newString =
                    "SELECT ${MatchDatabases.MATCH_ID}, ${MatchDatabases.MAP}, ${MatchDatabases.GAMEMODE} FROM ${MatchDatabases.USERMATCHES} WHERE ${MatchDatabases.USER} = '${user}'"
                val db = matchDatabse.readableDatabase
                val matchDCursor = db.rawQuery(newString, null)
                if (matchDCursor.moveToFirst()) do {
                    playersRef.child(userFinal[0]).child("Tag").setValue(userFinal[1])
                    playersRef.child(userFinal[0]).child("Matches").child(matchDCursor.getString(0))
                        .child("Map").setValue(matchDCursor.getString(1))
                    playersRef.child(userFinal[0]).child("Matches").child(matchDCursor.getString(0))
                        .child("Mode").setValue(matchDCursor.getString(2))
                } while (matchDCursor.moveToNext())
                matchDCursor.close()
                db.close()
            }

        } catch (e: Exception) {
            val contextView = findViewById<View>(R.id.MMRFAB)
            val snackbar = Snackbar
                .make(contextView, "Database not synced!", Snackbar.LENGTH_SHORT)
            snackbar.show()
            Log.d("database", e.toString())
        }
    }
}

