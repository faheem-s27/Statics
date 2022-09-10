package com.jawaadianinc.valorant_stats.valo.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
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
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.LiveMatchService
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsAgentsActivity
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsListActivity
import com.jawaadianinc.valorant_stats.valo.databases.MatchDatabase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.jawaadianinc.valorant_stats.valo.match_info.MatchHistoryActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.*

@SuppressLint("SetTextI18n")
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

        // keeps screen on
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

        val playersRef = database.getReference("VALORANT/players")
        playersRef.child(nameSplit[0]).child("Puuid").setValue(puuid)
        playersRef.child(nameSplit[0]).child("GameTag").setValue(nameSplit[1])
        playersRef.child(nameSplit[0]).child("Region").setValue(region)

        val agentsCozBT = findViewById<Button>(R.id.agentsCozBT)
        val weaponsBT = findViewById<Button>(R.id.weaponsBT)
        val mmrFAB: FloatingActionButton = findViewById(R.id.MMRFAB)
        val logOutFAB: FloatingActionButton = findViewById(R.id.RSOLogOut)
        val recentMatchFAB: FloatingActionButton = findViewById(R.id.RecentMatchFAB)
        val sharePlayerProfile: FloatingActionButton = findViewById(R.id.sharePlayerProfile)
        val playerNameText: TextView = findViewById(R.id.playerNameMenu)
        val optionsFAB: FloatingActionButton = findViewById(R.id.fabPlus)
        val seekBar: SeekBar = findViewById(R.id.howManyMatches)
        val liveMatchSwitch: SwitchMaterial = findViewById(R.id.liveMatch)
        val trackerGGButton: FloatingActionButton = findViewById(R.id.buildTrackerGGProfile)
        val crosshairButton: Button = findViewById(R.id.crosshairBT)

        val dimmed = findViewById<LinearLayout>(R.id.dim_layout)

        val fabRefresh: FloatingActionButton = findViewById(R.id.refreshFAB)

        fabRefresh.setOnClickListener {
            // this will restart the activity
            finish()
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }


        crosshairButton.setOnClickListener {
            // show alert dialog sayng that crosshair is not available due to changes in the game
            val dialog = AlertDialog.Builder(this)
            dialog.setTitle("Crosshair not available")
            dialog.setMessage("Crosshair is not available due to the new crosshair functions in Valorant 5.04.\n\nThis will need to be remade in Statics.")
            dialog.setPositiveButton("OK") { _, _ -> }
            dialog.show()
            //startActivity(Intent(this, CrossHairActivity::class.java))
        }

        val layer: ConstraintLayout = findViewById(R.id.constraintLayout)
        val listofViews = arrayListOf<View>()
        layer.childCount.let {
            for (i in 0 until it) {
                val v = layer.getChildAt(i)
                listofViews.add(v)
            }
        }

        val random = Random()
        val shuffled = listofViews.shuffled(random)

        val animateNameText: TextView = findViewById(R.id.animateName)
        animateNameText.text = "Welcome ${nameSplit[0]}\nWe are Valorant"
        disappearViews(listofViews)
        doAsync {
            Thread.sleep(2000)
            uiThread {
                // aniamte textview alpha to 0 with end action to animateviews
                animateNameText.animate().alpha(0f).setDuration(1000).start()
                animateViews(shuffled)
            }
        }
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

        findViewById<Button>(R.id.buddiesBT).setOnClickListener {
            val intent = Intent(this, CosmeticsListActivity::class.java)
            intent.putExtra("cosmetic", "buddies")
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        findViewById<Button>(R.id.mapsButton).setOnClickListener {
            val intent = Intent(this, CosmeticsListActivity::class.java)
            intent.putExtra("cosmetic", "maps")
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        findViewById<Button>(R.id.ranksButton).setOnClickListener {
            val intent = Intent(this, CosmeticsListActivity::class.java)
            intent.putExtra("cosmetic", "ranks")
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        findViewById<Button>(R.id.levelBordersBT).setOnClickListener {
            val intent = Intent(this, CosmeticsListActivity::class.java)
            intent.putExtra("cosmetic", "borders")
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        // ------------------------ End of cosmetics ------------------------------------

        trackerGGButton.setOnClickListener()
        {
            checkForTrackerGG(nameSplit[0], nameSplit[1])
        }

        // check if the LiveMatchService is running
        val serviceRunning = isServiceRunning(LiveMatchService::class.java)
        liveMatchSwitch.isChecked = serviceRunning

        liveMatchSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // show alert dialog to ask for confirmation
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Widget & Notification Details")
                builder.setMessage(
                    "This will start the Live Match Service, that will run every minute to check for recently played matches.\n" +
                            "A notification will be displayed for each new match played.\nAny active widgets will update every 30 mins\n\n" +
                            "You can disable this service anytime."
                )
                builder.setPositiveButton("Yes") { _, _ ->
                    // continue with Live Match
                    val progressDialog =
                        ProgressDialogStatics().setProgressDialog(this, "Fetching last match...")
                    progressDialog.show()
                    val url =
                        "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
                    doAsync {
                        val response = JSONObject(URL(url).readText()).getJSONArray("history")
                            .get(0) as JSONObject
                        gameStarted = response.getString("gameStartTimeMillis")
                        val matchID = response.getString("matchId")
                        val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"
                        val matchJSON = henrikAPI(matchURl)
                        val matchDB = MatchDatabase(applicationContext)
                        if (!matchDB.insertMatch(matchID, matchJSON.toString())) {
                            Log.d("MatchDatabase", "Match Database Insert Error")
                        }

                        Thread.sleep(1000)
                        uiThread {
                            val intent = Intent(this@ValorantMainMenu, LastMatchWidget::class.java)
                            intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                            val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                                ComponentName(applicationContext, LastMatchWidget::class.java)
                            )
                            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                            sendBroadcast(intent)

                            startLiveMatches(gameStarted, matchID)
                            progressDialog.dismiss()
                        }
                    }
                }
                builder.setNegativeButton("No") { _, _ ->
                    liveMatchSwitch.isChecked = false
                    try {
                        MatchDatabase(applicationContext).deleteMatch()
                        val widgetIntent =
                            Intent(this@ValorantMainMenu, LastMatchWidget::class.java)
                        widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                            ComponentName(applicationContext, LastMatchWidget::class.java)
                        )
                        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                        sendBroadcast(widgetIntent)

                        val intent = Intent(this, LiveMatchService::class.java)
                        stopService(intent)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                builder.show()
            } else {
                try {
                    MatchDatabase(applicationContext).deleteMatch()
                    val widgetIntent = Intent(this@ValorantMainMenu, LastMatchWidget::class.java)
                    widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                        ComponentName(applicationContext, LastMatchWidget::class.java)
                    )
                    widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    sendBroadcast(widgetIntent)

                    val intent = Intent(this, LiveMatchService::class.java)
                    stopService(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        var show = true

        logOutFAB.translationY = 600f
        sharePlayerProfile.translationY = 400f
        fabRefresh.translationY = 200f
        logOutFAB.visibility = View.INVISIBLE
        sharePlayerProfile.visibility = View.INVISIBLE
        fabRefresh.visibility = View.INVISIBLE

        optionsFAB.setOnClickListener {
            if (show) {
                // show the other FAB options
                logOutFAB.visibility = View.VISIBLE
                sharePlayerProfile.visibility = View.VISIBLE
                fabRefresh.visibility = View.VISIBLE
                optionsFAB.animate().rotationBy(45f).duration = 200
                logOutFAB.animate().alpha(1f).translationYBy(-600f).duration = 400
                sharePlayerProfile.animate().alpha(1f).translationYBy(-400f).duration = 300
                fabRefresh.animate().alpha(1f).translationYBy(-200f).duration = 200
                show = false
                logOutFAB.isClickable = true
                sharePlayerProfile.isClickable = true
                fabRefresh.isClickable = true
                //aniamte dimmed from 0 to 1 alpha in 200ms
                dimmed.animate().alpha(1f).duration = 200
            } else {
                // hide the FAB options
                optionsFAB.animate().rotationBy(-45f).duration = 200
                logOutFAB.animate().alpha(0f).translationYBy(600f).duration = 400
                sharePlayerProfile.animate().alpha(0f).translationYBy(400f).duration = 300
                fabRefresh.animate().alpha(0f).translationYBy(200f).duration = 200
                logOutFAB.isClickable = false
                sharePlayerProfile.isClickable = false
                fabRefresh.isClickable = false
                show = true
                //aniamte dimmed from 1 to 0 alpha in 200ms
                dimmed.animate().alpha(0f).duration = 200
            }
        }

        playerNameText.text = playerName

        logOutFAB.setOnClickListener {
            val playerDB = PlayerDatabase(this)
            if (playerDB.logOutPlayer(nameSplit[0])) {
                val widgetIntent = Intent(this, LastMatchWidget::class.java)
                widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                    ComponentName(applicationContext, LastMatchWidget::class.java)
                )
                widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                sendBroadcast(widgetIntent)

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
                .setMessage(R.string.Share_Profile_URL)
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

        recentMatchFAB.setOnClickListener {
            val puuid = PlayerDatabase(this).getPUUID(nameSplit[0], nameSplit[1])
            val region = PlayerDatabase(this).getRegion(puuid = puuid!!)
            val intent = Intent(this, ViewMatches::class.java)
            intent.putExtra("Region", region)
            intent.putExtra("PUUID", puuid)
            intent.putExtra("NumberOfMatches", seekBar.progress.toString())
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        mmrFAB.setOnClickListener {
            val name1 = name?.split("#")
            val intent = Intent(this@ValorantMainMenu, MMRActivity::class.java)
            intent.putExtra("RiotName", name1?.get(0))
            intent.putExtra("RiotID", name1?.get(1))
            intent.putExtra("key", key)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }


        getPlayerCards(nameSplit[0], nameSplit[1])
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

    private fun isServiceRunning(java: Class<LiveMatchService>): Boolean {
        val manager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE)) {
            if (java.name == service.service.className) {
                return true
            }
        }
        return false
    }

    private fun checkForTrackerGG(gameName: String, gameTag: String) {
        // show loading dialog
        val progressDialog =
            ProgressDialogStatics().setProgressDialog(this, "Checking for eligibility...")
        progressDialog.show()
        doAsync {
            try {
                val json = TrackerGGScraper().getProfile(gameName, gameTag)
                val privacy =
                    json.getJSONObject("data").getJSONObject("metadata").getString("privacy")
                if (privacy == "public") {
                    uiThread {
                        progressDialog.dismiss()
                        startTrackerGG(gameName, gameTag)
                    }
                } else {
                    uiThread {
                        progressDialog.dismiss()
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

    private fun startLiveMatches(gameStart: String, matchID: String) {
        val intent = Intent(this, LiveMatchService::class.java)
        intent.putExtra("gameStart", gameStart)
        intent.putExtra("key", key)
        intent.putExtra("puuid", puuid)
        intent.putExtra("region", region)
        intent.putExtra("matchID", matchID)
        startForegroundService(intent)
    }

    private fun startTrackerGG(name: String, tag: String) {
        Toast.makeText(this, "$name#$tag is eligible! Coming in 2.3 update", Toast.LENGTH_LONG)
            .show()
//        val intent = Intent(this, TrackerGG::class.java)
//        intent.putExtra("name", name)
//        intent.putExtra("tag", tag)
//        startActivity(intent)
    }

    private fun signIntoTrackerGG(name: String, tag: String) {
        val signInURL = "https://tracker.gg/valorant/profile/riot/$name%23$tag/overview"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(signInURL)
        startActivity(intent)
    }

    override fun onResume() {
        super.onResume()
        FirebaseApp.initializeApp(/*context=*/this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }

    private fun getPlayerCards(RiotName: String, RiotID: String) {
        val playerProfile: ImageView = findViewById(R.id.playerProfile)
        val playerLevel: TextView = findViewById(R.id.playerLevel)
        doAsync {
            val data =
                henrikAPI("https://api.henrikdev.xyz/valorant/v1/account/${RiotName}/${RiotID}?force=true")["data"] as JSONObject
            val largePic = data.getJSONObject("card").getString("large") as String
            val smolPic = data.getJSONObject("card").getString("small") as String
            uiThread {
                Picasso.get().load(smolPic).fit().centerInside().into(playerProfile)
                Picasso.get().load(largePic)
                    .transform(BlurTransformation(this@ValorantMainMenu)).fit().centerInside()
                    .into(imagebackground)
                playerLevel.text = data.getInt("account_level").toString()
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
            try {
                val currentTierData = henrikAPI(currentTier)
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
                        uiThread {
                            rankImageMainMenu.visibility = View.VISIBLE
                            Picasso
                                .get()
                                .load(tierIcon)
                                .fit()
                                .centerInside()
                                .into(rankImageMainMenu)
                            rankPatchedMainMenu.text = patched
                            rankProgressMainMenu.progress = progressNumber
                            rankProgressMainMenu.visibility = View.VISIBLE
                            rankNumberMainMenu.text = "$progressNumber/100"
                            rankNumberMainMenu.visibility = View.VISIBLE
                        }
                        break
                    }
                }
            } catch (e: Exception) {
                uiThread {
                    rankImageMainMenu.visibility = View.GONE
                    rankPatchedMainMenu.text = "Error in getting rank"
                    rankProgressMainMenu.visibility = View.GONE
                    rankNumberMainMenu.visibility = View.GONE
                }

                Log.d("Henrik", "Error: $e")
            }
        }
    }

    private fun getLastMatch(RiotName: String, RiotID: String) {
        val lastMatchMapImage: ImageView = findViewById(R.id.lastMatchMapImage)
        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/eu/$RiotName/$RiotID?size=1"
        val agentImageMainMenu: ImageView = findViewById(R.id.agentImageMainMenu)
        doAsync {
            try {
                val lastMatchData = henrikAPI(allmatches)
                val jsonOfMap = JSONObject(URL("https://valorant-api.com/v1/maps").readText())
                val mapData = jsonOfMap["data"] as JSONArray
                var actualtMapUlr = ""
                val data = lastMatchData["data"] as JSONArray
                val metadata = data.getJSONObject(0).getJSONObject("metadata")
                val map = metadata.getString("map")

                val unixTimeStart = metadata.getInt("game_start")
                val date = Date(unixTimeStart.toLong() * 1000)
                val d: Duration =
                    Duration.between(
                        date.toInstant(),
                        Instant.now()
                    )

                var kda: String? = null

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
                        kda =
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
                        break
                    }
                }
                uiThread {
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
                    lastMatchStatsMainMenu.text = kda
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
            } catch (e: Exception) {
                uiThread {
                    Log.d("Henrik", "Error: $e")
                    val lastMatchStatsMainMenu: TextView = findViewById(R.id.lastMatchStatsMainMenu)
                    lastMatchStatsMainMenu.text = "Error loading data"
                }
            }
        }
    }

    private fun seekBarMatches(key: String) {
        val nameSplit = playerName.split("#")
        val puuid = PlayerDatabase(this@ValorantMainMenu).getPUUID(nameSplit[0], nameSplit[1])
        val region = PlayerDatabase(this@ValorantMainMenu).getRegion(puuid!!)
        val seekBar: SeekBar = findViewById(R.id.howManyMatches)
        val url =
            "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"

        doAsync {
            val number = JSONObject(URL(url).readText()).getJSONArray("history").length()
            uiThread {
                // the limit of seekbar is 60, if user has played less than 60 matches, the limit is the number of matches played
                seekBar.max = if (number < 60) number else 60
                seekBar.progress = 5
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

    private fun animateViews(view: List<View>) {
        var reverseDirection = false
        var delay = 0L
        for (i in view.indices) {
            // make reverse direction every other time
            if (i % 2 == 0) {
                reverseDirection = !reverseDirection
            }
            val v = view[i]
            v.alpha = 0f
            if (reverseDirection) {
                v.translationX = 500f
                v.animate().alpha(1f).setDuration(500).translationXBy(-500f).startDelay = delay
            } else {
                v.translationX = -500f
                v.animate().alpha(1f).setDuration(500).translationXBy(500f).startDelay = delay
            }
            //v.translationX = -y
            delay += 50L
        }
    }

    private fun disappearViews(view: List<View>) {
        for (i in view.indices) {
            val v = view[i]
            v.alpha = 0f
        }
    }

    private fun henrikAPI(playerURL: String): JSONObject {
        return executeRequest(playerURL)
    }

    private fun executeRequest(playerURL: String): JSONObject {
        val client = OkHttpClient()
        val urlBuilder: HttpUrl.Builder =
            playerURL.toHttpUrlOrNull()!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "HDEV-67e86af9-8bf9-4f6d-b628-f4521b20d772")
            .build()
        val call = client.newCall(request).execute()
        // log the call headers
        // Log.d("Henrik", call.headers.toString())
        return JSONObject(call.body.string())
    }
}
