package com.jawaadianinc.valorant_stats.valorant

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
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

class RecieveSharedProfileActivity : AppCompatActivity() {

    private lateinit var imagebackground: ImageView
    private lateinit var gameNamePlayer: String
    private lateinit var tagLinePlayer: String

    private lateinit var playerPUUID: String
    private lateinit var regionPlayer: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaccount)
        val data: Uri? = intent?.data

        val MMR: FloatingActionButton = findViewById(R.id.MMRFAB)
        val RSOLogOut: Button = findViewById(R.id.RSOLogOut)
        val RecentMatchFAB: FloatingActionButton = findViewById(R.id.RecentMatchFAB)
        val sharePlayerProfile: Button = findViewById(R.id.sharePlayerProfile)
        val goBackFromLink: FloatingActionButton = findViewById(R.id.goBackFromLink)

        goBackFromLink.visibility = View.VISIBLE
        goBackFromLink.setOnClickListener {
            val intent = Intent(this, ValorantMainMenu::class.java)
            startActivity(intent)
        }

        RecentMatchFAB.setOnClickListener {
            val intent = Intent(this, ViewMatches::class.java)
            intent.putExtra("Region", regionPlayer)
            intent.putExtra("PUUID", playerPUUID)
            startActivity(intent)
        }

        MMR.setOnClickListener {
            val intent = Intent(this, MMRActivity::class.java)
            intent.putExtra("RiotName", gameNamePlayer)
            intent.putExtra("RiotID", tagLinePlayer)
            startActivity(intent)
        }

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Verifying Account")
        progressDialog.setMessage("Collecting details...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.show()


        if (data != null) {
            val split = data.toString().split("/")
            regionPlayer = split[5]
            playerPUUID = split[6]
            sharePlayerProfile.visibility = View.INVISIBLE
            RSOLogOut.visibility = View.INVISIBLE
        } else {
            Toast.makeText(this, "Not valid link", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, GamePickerMenu::class.java))
            finish()
        }

        val database = Firebase.database
        val myRef = database.getReference("VALORANT/key")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val key = dataSnapshot.value as String?
                setPlayerBackground(key!!)
                progressDialog.dismiss()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setPlayerBackground(key: String) {
        imagebackground = findViewById(R.id.imagebackground)
        doAsync {
            try {
                val urlName =
                    "https://europe.api.riotgames.com/riot/account/v1/accounts/by-puuid/$playerPUUID?api_key=$key"
                gameNamePlayer = JSONObject(URL(urlName).readText()).getString("gameName")
                tagLinePlayer = JSONObject(URL(urlName).readText()).getString("tagLine")

                val recentDB = RecentPlayersDB(this@RecieveSharedProfileActivity)
                runOnUiThread {
                    if (recentDB.addRecentPlayer(
                            gameNamePlayer,
                            tagLinePlayer,
                            playerPUUID,
                            regionPlayer
                        )
                    ) {
                        Toast.makeText(
                            this@RecieveSharedProfileActivity,
                            "Player received: $gameNamePlayer#$tagLinePlayer",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        throw Exception("Error")
                    }
                    val playerNameText: TextView = findViewById(R.id.playerNameMenu)
                    playerNameText.text = "Shared from link\n$gameNamePlayer#$tagLinePlayer"
                    getRank(gameNamePlayer, tagLinePlayer)
                    getLastMatch(gameNamePlayer, tagLinePlayer)
                }

                val data =
                    JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/$gameNamePlayer/${tagLinePlayer}?force=true").readText())["data"] as JSONObject
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
                runOnUiThread {
                    Toast.makeText(
                        this@RecieveSharedProfileActivity,
                        "Invalid player link!",
                        Toast.LENGTH_SHORT
                    ).show()
                    startActivity(
                        Intent(
                            this@RecieveSharedProfileActivity,
                            GamePickerMenu::class.java
                        )
                    )
                    finish()
                }
            }
        }
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

}
