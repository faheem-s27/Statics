package com.jawaadianinc.valorant_stats.valo

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.TrackerGGScraper
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsAgentsActivity
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsListActivity
import com.jawaadianinc.valorant_stats.valo.match_info.MatchHistoryActivity
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
    private var playerName = ""
    private var key = ""
    private var region = ""
    private var puuid = ""
    private var gameStarted = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaccount)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val name = PlayerDatabase(this).getPlayerName()
        if (name == null) {
            startActivity(Intent(this, LoggingInActivityRSO::class.java))
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            Toast.makeText(this, "Sign in to continue!", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            playerName = name
        }
        val database = Firebase.database

        if (playerName == "") {
            startActivity(Intent(this, LoggingInActivityRSO::class.java))
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            Toast.makeText(this, "Sign in to continue!", Toast.LENGTH_SHORT).show()
            finish()
        }

        Log.d("Player Name", playerName)


        val nameSplit = playerName.split("#")
        puuid = PlayerDatabase(this).getPUUID(nameSplit[0], nameSplit[1])!!
        region = PlayerDatabase(this).getRegion(puuid)!!
        try {
            key = intent.extras!!.getString("key")!!
        } catch (e: Exception) {
            val keyRef = database.getReference("VALORANT/key")
            keyRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    key = (dataSnapshot.value as String?).toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        val playersRef = database.getReference("VALORANT/signedInPlayers")
        playersRef.child(nameSplit[0]).child("Puuid").setValue(puuid)
        playersRef.child(nameSplit[0]).child("GameTag").setValue(nameSplit[1])
        playersRef.child(nameSplit[0]).child("Region").setValue(region)

        val agentsCozBT = findViewById<Button>(R.id.agentsCozBT)
        val weaponsBT = findViewById<Button>(R.id.weaponsBT)
        val MMR: FloatingActionButton = findViewById(R.id.MMRFAB)
        val RSOLogOut: FloatingActionButton = findViewById(R.id.RSOLogOut)
        val RecentMatchFAB: FloatingActionButton = findViewById(R.id.RecentMatchFAB)
        val sharePlayerProfile: FloatingActionButton = findViewById(R.id.sharePlayerProfile)
        val playerNameText: TextView = findViewById(R.id.playerNameMenu)
        val FABplus: FloatingActionButton = findViewById(R.id.fabPlus)
        val seekBar: SeekBar = findViewById(R.id.howManyMatches)
        val liveMatchSwitch: SwitchMaterial = findViewById(R.id.liveMatch)
        val trackerGGButton: Button = findViewById(R.id.buildTrackerGGProfile)


        val layer: ConstraintLayout = findViewById(R.id.constraintLayout)
        val listofViews = arrayListOf<View>()
        layer.childCount.let {
            for (i in 0 until it) {
                val v = layer.getChildAt(i)
                listofViews.add(v)
            }
        }

        animateViews(listofViews, 500, 400F)
        imagebackground = findViewById(R.id.imagebackground)

        // ------------------------ Cosmetics ------------------------------------
        agentsCozBT.setOnClickListener {
            val intent = Intent(this, CosmeticsAgentsActivity::class.java)
            intent.putExtra("data", "agent")
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }
        weaponsBT.setOnClickListener {
            val intent = Intent(this, CosmeticsListActivity::class.java)
            intent.putExtra("cosmetic", "weapon")
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        findViewById<Button>(R.id.playerCardsBT).setOnClickListener {
            val intent = Intent(this, CosmeticsListActivity::class.java)
            intent.putExtra("cosmetic", "cards")
            intent.putExtra("size", "large")
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        findViewById<Button>(R.id.spraysBT).setOnClickListener {
            val intent = Intent(this, CosmeticsListActivity::class.java)
            intent.putExtra("cosmetic", "sprays")
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        // ------------------------ End of cosmetics ------------------------------------

        trackerGGButton.setOnClickListener()
        {
            checkForTrackerGG(nameSplit[0], nameSplit[1])
        }

        liveMatchSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // show alert dialog to ask for confirmation
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Match Notification Details")
                builder.setMessage(
                    "Your device will continuously check for any new matches played from now\nWhen a new match has been played, a notification will be sent to your device for you to view details of that match\n" +
                            "Are you sure you want to enable this?"
                )
                builder.setPositiveButton("Yes") { _, _ ->
                    // continue with Live Match
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setTitle("Setting up")
                    progressDialog.setMessage("Please wait while we set up your account for match notifications")
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    progressDialog.setCancelable(false)
                    progressDialog.show()
                    val URL =
                        "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
                    doAsync {
                        val response = JSONObject(URL(URL).readText()).getJSONArray("history")
                            .get(0) as JSONObject
                        gameStarted = response.getString("gameStartTimeMillis")
                        Thread.sleep(1000)
                        uiThread {
                            startLiveMatches(gameStarted)
                            progressDialog.dismiss()
                        }
                    }
                }
                builder.setNegativeButton("No") { _, _ ->
                    liveMatchSwitch.isChecked = false
                    try {
                        val intent = Intent(this, LiveMatchService::class.java)
                        stopService(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                builder.show()
            } else {
                try {
                    val intent = Intent(this, LiveMatchService::class.java)
                    stopService(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        var show = true

        RSOLogOut.translationY = 200f
        sharePlayerProfile.translationY = 200f
        RSOLogOut.visibility = View.INVISIBLE
        sharePlayerProfile.visibility = View.INVISIBLE

        FABplus.setOnClickListener {
            if (show) {
                // show the other FAB options
                RSOLogOut.visibility = View.VISIBLE
                sharePlayerProfile.visibility = View.VISIBLE
                FABplus.animate().rotationBy(405f).duration = 200
                RSOLogOut.animate().alpha(1f).translationYBy(-200f).duration = 200
                sharePlayerProfile.animate().alpha(1f).translationYBy(-200f).duration = 200
                show = false
                RSOLogOut.isClickable = true
                sharePlayerProfile.isClickable = true
            } else {
                // hide the FAB options
                FABplus.animate().rotationBy(-405f).duration = 200
                RSOLogOut.animate().alpha(0f).translationYBy(200f).duration = 200
                sharePlayerProfile.animate().alpha(0f).translationYBy(200f).duration = 200
                RSOLogOut.isClickable = false
                sharePlayerProfile.isClickable = false
                show = true
            }
        }

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

            AlertDialog.Builder(this).setTitle("Disclaimer!")
                .setMessage(
                    "Anyone with access to the link can view your matches and rank. " +
                            "If you are not sure if you want to share your profile, please do not share it!"
                )
                .setPositiveButton("Share") { _, _ ->
                    val url =
                        Uri.parse("https://statics-fd699.web.app/valorant/profile/$region/$puuid")
                    val sendIntent: Intent = Intent().apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, url.toString())
                        type = "text/plain"
                    }

                    val shareIntent =
                        Intent.createChooser(sendIntent, "Share Valorant profile using")
                    startActivity(shareIntent)

                }
                .setNegativeButton("Cancel") { _, _ ->
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }

        RecentMatchFAB.setOnClickListener {
            val puuid = PlayerDatabase(this).getPUUID(nameSplit[0], nameSplit[1])
            val region = PlayerDatabase(this).getRegion(puuid = puuid!!)
            val intent = Intent(this, ViewMatches::class.java)
            intent.putExtra("Region", region)
            intent.putExtra("PUUID", puuid)
            intent.putExtra("NumberOfMatches", seekBar.progress.toString())
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        MMR.setOnClickListener {
            val fullname = name
            val name1 = fullname?.split("#")
            val intent = Intent(this@ValorantMainMenu, MMRActivity::class.java)
            intent.putExtra("RiotName", name1?.get(0))
            intent.putExtra("RiotID", name1?.get(1))
            intent.putExtra("key", key)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

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


        getRank(nameSplit[0], nameSplit[1])
        getLastMatch(nameSplit[0], nameSplit[1])
        seekBarMatches(key)
        val howManyMatches: TextView = findViewById(R.id.textView7)

        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                howManyMatches.text = "See last $progress matches"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })

    }

    private fun checkForTrackerGG(gameName: String, gameTag: String) {
        // show loading dialog
        val progressDialog = ProgressDialog(this)
        progressDialog.setMessage("Checking if eligible...")
        progressDialog.show()
        progressDialog.setCancelable(false)
        doAsync {
            try {
                val json = TrackerGGScraper().getProfile(gameName, gameTag)
                val privacy =
                    json.getJSONObject("data").getJSONObject("metadata").getString("privacy")
                uiThread {
                    progressDialog.dismiss()
                    if (privacy == "public") {
                        startTrackerGG(gameName, gameTag)
                    } else {
                        // show dialog saying player is not signed in at tracker.gg
                        val builder = AlertDialog.Builder(this@ValorantMainMenu)
                        builder.setTitle("Profile is private")
                        builder.setMessage("To continue, you need to be signed in at tracker.gg and have your profile set to public.")
                        builder.setPositiveButton("Sign in") { _, _ ->
                            signIntoTrackerGG(gameName, gameTag)
                        }
                        builder.setNegativeButton("Cancel") { _, _ ->
                            Toast.makeText(
                                this@ValorantMainMenu,
                                "Request cancelled",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                        builder.show()
                    }
                }

            } catch (e: Exception) {
                uiThread {
                    progressDialog.dismiss()
                    Toast.makeText(this@ValorantMainMenu, "Error: $e", Toast.LENGTH_LONG).show()
                    Log.d("TrackerGG", "Error: $e")
                }
            }
        }

    }

    private fun startLiveMatches(gameStart: String) {
        val intent = Intent(this, LiveMatchService::class.java)
        intent.putExtra("gameStart", gameStart)
        intent.putExtra("key", key)
        intent.putExtra("puuid", puuid)
        intent.putExtra("region", region)
        startForegroundService(intent)
    }

    private fun startTrackerGG(name: String, tag: String) {
        Toast.makeText(this, "Building profile", Toast.LENGTH_LONG).show()
//        val intent = Intent(this, TrackerGG::class.java)
//        intent.putExtra("name", name)
//        intent.putExtra("tag", tag)
//        startActivity(intent)

    }

    private fun signIntoTrackerGG(name: String, tag: String) {
        val SignInUrl = "https://tracker.gg/valorant/profile/riot/$name%23$tag/overview"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(SignInUrl)
        startActivity(intent)
    }

    private fun seekBarMatches(key: String) {
        val nameSplit = playerName.split("#")
        val puuid = PlayerDatabase(this).getPUUID(nameSplit[0], nameSplit[1])
        val region = PlayerDatabase(this).getRegion(puuid!!)

        val seekBar: SeekBar = findViewById(R.id.howManyMatches)
        val URL =
            "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"

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

            val number = JSONObject(URL(URL).readText()).getJSONArray("history").length()
            uiThread {
                seekBar.max = number
                seekBar.progress = 5
            }
        }
    }

    override fun onResume() {
        FirebaseApp.initializeApp(/*context=*/this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
        super.onResume()
    }

    @SuppressLint("SetTextI18n")
    private fun getRank(RiotName: String, RiotID: String) {
        val rankImageMainMenu: ImageView = findViewById(R.id.rankImageMainMenu)
        val rankPatchedMainMenu: TextView = findViewById(R.id.rankPatchedMainMenu)
        val rankProgressMainMenu: ProgressBar = findViewById(R.id.rankProgressMainMenu)
        val rankNumberMainMenu: TextView = findViewById(R.id.rankNumberMainMenu)

        val tierURL = "https://valorant-api.com/v1/competitivetiers"
        val currentTier = "https://api.henrikdev.xyz/valorant/v1/mmr/eu/${RiotName}/$RiotID"

        doAsync {
            try {
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
            } catch (e: Exception) {
                Log.d("Henrik", "Error: $e")
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
            val date = Date(unixTimeStart.toLong() * 1000)
            val d: Duration =
                Duration.between(
                    date.toInstant(),
                    Instant.now()
                )

            var KDA: String? = null

            val gameModeMainMenu: TextView = findViewById(R.id.gameModeMainMenu)
            val gameMode = metadata.getString("mode")
            val matchID = metadata.getString("matchid")

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
                lastMatchMapImage.setOnClickListener {
                    matchActivityStart(RiotName, RiotID, matchID)
                }

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

    private fun matchActivityStart(Name: String, ID: String, matchID: String) {
        val matchintent = Intent(this, MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", 0)
        matchintent.putExtra("MatchID", matchID)
        startActivity(matchintent)
    }

    private fun animateViews(view: List<View>, duration: Long, y: Float) {
        var delay = 0L
        for (i in view.indices) {
            val v = view[i]
            v.alpha = 0f
            v.translationY = -y
            v.animate().alpha(1f).setDuration(duration).translationYBy(y).startDelay = delay
            delay += 50L
        }
    }
}



