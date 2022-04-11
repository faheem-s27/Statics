package com.jawaadianinc.valorant_stats.valorant

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*


class ValorantMainMenu : AppCompatActivity() {
    private lateinit var imagebackground: ImageView
    private lateinit var playerName: String
    private var key = ""

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
        imagebackground = findViewById(R.id.imagebackground)

        val puuid = PlayerDatabase(this).getPUUID(nameSplit[0], nameSplit[1])
        val region = PlayerDatabase(this).getRegion(puuid!!)

        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/signedInPlayers")
        playersRef.child(nameSplit[0]).child("Puuid").setValue(puuid)
        playersRef.child(nameSplit[0]).child("GameTag").setValue(nameSplit[1])
        playersRef.child(nameSplit[0]).child("Region").setValue(region)

        val myRef = database.getReference("VALORANT/key")
        myRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                key = (dataSnapshot.value as String?).toString()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

        doAsync {
            try {
                val data =
                    JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${nameSplit[0]}/${nameSplit[1]}?force=true").readText())["data"] as JSONObject
                val largePic = data.getJSONObject("card").getString("large") as String
                val smolPic = data.getJSONObject("card").getString("small") as String
                val playerProfile: ImageView = findViewById(R.id.playerProfile)
                val playerLevel: TextView = findViewById(R.id.playerLevel)
                uiThread {
                    Picasso.get().load(smolPic).fit().centerInside().into(playerProfile)
                    Picasso.get().load(largePic)
                        .transform(BlurTransformation(this@ValorantMainMenu)).fit().centerInside()
                        .into(imagebackground)
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
        val sharePlayerProfile: Button = findViewById(R.id.sharePlayerProfile)
        val playerNameText: TextView = findViewById(R.id.playerNameMenu)
        playerNameText.text = playerName

        RSOLogOut.setOnClickListener {
            val playerDB = PlayerDatabase(this)
            if (playerDB.logOutPlayer(nameSplit[0])) {
                startActivity(Intent(this, LoggingInActivityRSO::class.java))
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                finish()
                Toast.makeText(this, "Logged out!", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Error logging out O_o", Toast.LENGTH_SHORT).show()
            }
        }

        sharePlayerProfile.setOnClickListener {
            val puuid = PlayerDatabase(this).getPUUID(nameSplit[0], nameSplit[1])
            val region = PlayerDatabase(this).getRegion(puuid = puuid!!)

            val url = Uri.parse("https://statics-fd699.web.app/valorant/profile/$region/$puuid")
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_TEXT, url.toString())

            val intent = Intent.createChooser(shareIntent, "Share Valorant profile using")
            startActivity(intent)
        }

        RecentMatchFAB.setOnClickListener {
            val puuid = PlayerDatabase(this).getPUUID(nameSplit[0], nameSplit[1])
            val region = PlayerDatabase(this).getRegion(puuid = puuid!!)
            val intent = Intent(this, ViewMatches::class.java)
            intent.putExtra("Region", region)
            intent.putExtra("PUUID", puuid)
            startActivity(intent)
        }

        updatesButton.setOnClickListener {
            startActivity(Intent(this, ValorantUpdatesActivity::class.java))
        }

        MMR.setOnClickListener {
            val fullname = name
            val name = fullname?.split("#")
            val intent = Intent(this@ValorantMainMenu, MMRActivity::class.java)
            intent.putExtra("RiotName", name?.get(0))
            intent.putExtra("RiotID", name?.get(1))
            intent.putExtra("key", key)
            startActivity(intent)
        }

        leaderBoard.setOnClickListener {
            startActivity(Intent(this, leaderBoardActivity::class.java))
        }

        getRank(nameSplit[0], nameSplit[1])
        getLastMatch(nameSplit[0], nameSplit[1])

    }

    override fun onResume() {
        super.onResume()
        FirebaseApp.initializeApp(/*context=*/this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
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
                        //rankImageMainMenu.scaleType = ImageView.ScaleType.FIT_XY
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
            val date = Date(unixTimeStart.toLong() * 1000)
            val d: Duration =
                Duration.between(
                    date.toInstant(),
                    Instant.now()
                )

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
                    .transform(BlurTransformation(this@ValorantMainMenu, 2, 2))
                    .fit()
                    .centerInside()
                    .into(lastMatchMapImage)
            }
        }
    }

}

