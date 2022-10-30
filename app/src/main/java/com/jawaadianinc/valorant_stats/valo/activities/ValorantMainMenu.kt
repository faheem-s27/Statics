package com.jawaadianinc.valorant_stats.valo.activities

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ProgressDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
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
import com.jawaadianinc.valorant_stats.main.AboutActivity
import com.jawaadianinc.valorant_stats.main.LoadingActivity
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.LiveMatchService
import com.jawaadianinc.valorant_stats.valo.activities.chat.ChatsForumActivity
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsAgentsActivity
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsListActivity
import com.jawaadianinc.valorant_stats.valo.databases.MatchDatabase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.jawaadianinc.valorant_stats.valo.databases.TrackerDB
import com.jawaadianinc.valorant_stats.valo.live_match.LiveMatchesActivity
import com.jawaadianinc.valorant_stats.valo.match_info.MatchHistoryActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
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
    private var playerImage = ""

    private val scraper = TrackerGGScraper()

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

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val currentTime = Calendar.getInstance().time
        val time = currentTime.toString().split(" ")[3].split(":")
        val hour = time[0]
        val minute = time[1]
        val currentTimeString = "$hour:$minute"
        val playerRef = database.getReference("VALORANT/LastSeen")
        playerRef.child(nameSplit[0]).child("At")
            .setValue(currentTimeString + " " + dateFormat.format(Date()))
        playerRef.child(nameSplit[0]).child("Puuid").setValue(puuid)
        playerRef.child(nameSplit[0]).child("GameTag").setValue(nameSplit[1])
        playerRef.child(nameSplit[0]).child("Region").setValue(region)

        val agentsCozBT = findViewById<Button>(R.id.agentsCozBT)
        val weaponsBT = findViewById<Button>(R.id.weaponsBT)
        val mmrFAB: FloatingActionButton = findViewById(R.id.MMRFAB)
        val logOutFAB: FloatingActionButton = findViewById(R.id.RSOLogOut)
        val recentMatchFAB: FloatingActionButton = findViewById(R.id.RecentMatchFAB)
        val sharePlayerProfile: FloatingActionButton = findViewById(R.id.sharePlayerProfile)
        val playerNameText: TextView = findViewById(R.id.playerNameMenu)
        val optionsFAB: FloatingActionButton = findViewById(R.id.fabPlus)
        val seekBar: SeekBar = findViewById(R.id.howManyMatches)
        val notificationsSwitch: SwitchMaterial = findViewById(R.id.notifications)
        val trackerGGButton: Button = findViewById(R.id.buildTrackerGGProfile)
        val crosshairButton: Button = findViewById(R.id.crosshairBT)
        val dimmed = findViewById<LinearLayout>(R.id.dim_layout)
        val fabRefresh: FloatingActionButton = findViewById(R.id.refreshFAB)
        val aboutPage: Button = findViewById(R.id.AboutBT)
        val liveMatches = findViewById<Button>(R.id.LiveMatchBT)
        val ChatsForumButton = findViewById<Button>(R.id.ChatsForumButton)

        showLatestFeature(
            "Chat Bug Fix!",
            "Now you can **actually** use the chat feature 🦆❤️!", true
        )

        ChatsForumButton.setOnClickListener {
            val intent = Intent(this, ChatsForumActivity::class.java)
            intent.putExtra("playerName", playerName)
            intent.putExtra("playerImage", playerImage)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        liveMatches.setOnClickListener {
            // ask the user if they have the client installed on their PC/Laptop
            val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            builder.setTitle("Live Match")
            builder.setMessage("This feature requires the Statics client to be installed on your PC/Laptop. \n\nDo you have the Statics Client installed?")
            // set the colour of the buttons

            builder.setPositiveButton("Yes") { dialog, _ ->
                dialog.dismiss()
                startActivity(Intent(this, LiveMatchesActivity::class.java))
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }
            builder.setNegativeButton("No") { dialog, _ ->
                // open the website to download the client
                val browserIntent = Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://www.mediafire.com/file/hwfsrig47x015n6/Statics_Client.exe/file")
                )
                startActivity(browserIntent)
                dialog.dismiss()
            }
            builder.setNeutralButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            builder.show()
        }

        aboutPage.setOnClickListener {
            startActivity(Intent(this, AboutActivity::class.java))
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        fabRefresh.setOnClickListener {
            // this will restart the activity
            finish()
            startActivity(Intent(this, LoadingActivity::class.java))
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        // show an alert dialog that says thank you for using the app
        val alertDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        alertDialog.setTitle("Thank you ${playerName.split("#")[0]} for using Statics!")
        alertDialog.setMessage("If you have any suggestions or feedback, you can join the Statics discord server and send a message to the developer.\n\nOr you can share the app with your friends!")
        alertDialog.setPositiveButton("Join") { dialog, which ->
            // start an intent to open the discord server
            val browserIntent =
                Intent(Intent.ACTION_VIEW, Uri.parse("https://discord.gg/hgacc2kVMa"))
            startActivity(browserIntent)

            val discordRef = database.getReference("VALORANT/discord")
            discordRef.child(nameSplit[0]).child("Joined").setValue(true)

            dialog.dismiss()
        }
        alertDialog.setNegativeButton("Share") { dialog, which ->
            // share the app
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.type = "text/plain"
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Statics")
            var shareMessage = "This app shows you your Valorant stats!\n"
            shareMessage = """
                $shareMessage
                https://play.google.com/store/apps/details?id=com.jawaadianinc.valorant_stats
                """.trimIndent()
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
            startActivity(Intent.createChooser(shareIntent, "Choose one"))

            val discordRef = database.getReference("VALORANT/discord")
            discordRef.child(nameSplit[0]).child("Shared").setValue(true)

            dialog.dismiss()
        }
        alertDialog.setNeutralButton("Dismiss") { dialog, which ->
            // show the alert dialog again in 2 days
            val sharedPref = getSharedPreferences("alertDialog", Context.MODE_PRIVATE)
            val editor = sharedPref.edit()
            editor.putLong("lastTime", System.currentTimeMillis())
            editor.apply()
            // toast to say that the dialog will show again in 2 days
            Toast.makeText(this, "See you in 2 days! ;)", Toast.LENGTH_SHORT).show()

            val discordRef = database.getReference("VALORANT/discord")
            discordRef.child(nameSplit[0]).child("Joined").setValue(false)

            dialog.dismiss()
        }

        // add a Do not show again button view to the alert dialog
        val dontShowAgain = CheckBox(this)
        dontShowAgain.text = "Do not show again"
        dontShowAgain.setTextColor(Color.WHITE)
        alertDialog.setView(dontShowAgain)

        val sharedPref = getSharedPreferences("alertDialog", Context.MODE_PRIVATE)
        val firstTime = sharedPref.getBoolean("firstTime", true)
        val doNotShowAgain = sharedPref.getBoolean("doNotShowAgain", false)
        // if the user clicks the do not show again button, then add the value to shared preferences
        dontShowAgain.setOnCheckedChangeListener { _, isChecked ->
            val editor = sharedPref.edit()
            editor.putBoolean("doNotShowAgain", isChecked)
            editor.apply()
        }

        // get the value of doNotShowAgain and check if it is true or false
        if (!doNotShowAgain) {
            if (firstTime) {
                // show the alert dialog
                alertDialog.show()
                // set the first time to false
                val editor = sharedPref.edit()
                editor.putBoolean("firstTime", false)
                editor.apply()
            } else {
                // check if it has been 2 days
                val lastTime = sharedPref.getLong("lastTime", 0)
                val currentTime = System.currentTimeMillis()
                val difference = currentTime - lastTime

                val twoDays = 172800000
                if (difference > twoDays) {
                    // show the alert dialog
                    alertDialog.show()
                    // set the last time to the current time
                    val editor = sharedPref.edit()
                    editor.putLong("lastTime", System.currentTimeMillis())
                    editor.apply()
                }
            }
        }

        crosshairButton.setOnClickListener {
            // show alert dialog sayng that crosshair is not available due to changes in the game
            val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            dialog.setTitle("Crosshair unavailable")
            dialog.setMessage("Crosshair is currently disabled in the app due to the new crosshair functions in Valorant 5.04.\n\nThis will need to be remade in Statics.")
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
            intent.putExtra("cosmetic", "weapons")
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


        // check if the LiveMatchService is running
        val serviceRunning = isServiceRunning(LiveMatchService::class.java)
        notificationsSwitch.isChecked = serviceRunning

        // update the widget
        val intent =
            Intent(this, LastMatchWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(applicationContext, LastMatchWidget::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)

        notificationsSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // show alert dialog to ask for confirmation
                val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                builder.setTitle("Notification Details")
                builder.setMessage(
                    "This will start the Match Service, that will run every minute to check for recently played matches.\n" +
                            "A notification will be displayed for each new match played.\nAny active widgets will update every 30 mins\n\n" +
                            "You can disable this service anytime. Enable?"
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
                        val matchJSON = Henrik(this@ValorantMainMenu).henrikAPI(matchURl)
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
                    notificationsSwitch.isChecked = false
                    try {
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
                optionsFAB.animate().rotationBy(45f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 200
                logOutFAB.animate().alpha(1f).translationYBy(-600f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 400
                sharePlayerProfile.animate().alpha(1f).translationYBy(-400f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 300
                fabRefresh.animate().alpha(1f).translationYBy(-200f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 200
                show = false
                logOutFAB.isClickable = true
                sharePlayerProfile.isClickable = true
                fabRefresh.isClickable = true
                //aniamte dimmed from 0 to 1 alpha in 200ms
                dimmed.animate().alpha(1f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 200
            } else {
                // hide the FAB options
                optionsFAB.animate().rotationBy(-45f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 200
                logOutFAB.animate().alpha(0f).translationYBy(600f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 400
                sharePlayerProfile.animate().alpha(0f).translationYBy(400f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 300
                fabRefresh.animate().alpha(0f).translationYBy(200f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 200
                logOutFAB.isClickable = false
                sharePlayerProfile.isClickable = false
                fabRefresh.isClickable = false
                show = true
                //aniamte dimmed from 1 to 0 alpha in 200ms
                dimmed.animate().alpha(0f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.duration = 200
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

            AlertDialog.Builder(this, R.style.AlertDialogTheme).setTitle("Disclaimer!")
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

        trackerGGButton.setOnClickListener()
        {
            // show a dialog to say that these stats are updated once a day
            // only show this dialog once
            val prefs = getSharedPreferences("trackerGGDialog", Context.MODE_PRIVATE)
            val editor = prefs.edit()
            if (!prefs.getBoolean("shown", false)) {
                AlertDialog.Builder(this, R.style.AlertDialogTheme).setTitle("Disclaimer!")
                    .setMessage("These stats are updated only once a day, so they may not be accurate.")
                    .setPositiveButton("Ok") { _, _ ->
                        editor.putBoolean("shown", true)
                        editor.apply()
                        checkForTrackerGG(nameSplit[0], nameSplit[1])
                    }
                    .setNegativeButton("Cancel") { _, _ ->
                        Toast.makeText(this, "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
            } else {
                checkForTrackerGG(nameSplit[0], nameSplit[1])
            }
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

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update.
                val MY_REQUEST_CODE = 0
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    MY_REQUEST_CODE
                )
            }
        }
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

    private fun loadTrackerGG(gameName: String, gameTag: String, mode: String) {
        val progressDoalog = ProgressDialog(this)
        progressDoalog.max = 5
        progressDoalog.setMessage("Compiling stats...")
        progressDoalog.setTitle("$gameName#$gameTag's $mode stats")
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDoalog.show()

        val db = TrackerDB(this)

        doAsync {
            try {
                val json = scraper.getProfile(gameName, gameTag)
                uiThread {
                    progressDoalog.progress = 1
                }
                val privacy =
                    json.getJSONObject("data").getJSONObject("metadata").getString("privacy")
                if (privacy == "public") {
                    if (!db.checkIfDataExists(mode, playerName)) {
                        Log.d("TrackerGG", "Data for $mode does not exist, inserting")
                        scraper.getMaps(mode)
                        uiThread {
                            progressDoalog.progress = 2
                        }
                        scraper.getAgents(mode)
                        uiThread {
                            progressDoalog.progress = 3
                        }
                        scraper.getWeapons(mode)
                        uiThread {
                            progressDoalog.progress = 4
                        }

                        scraper.putToDatabase(mode, this@ValorantMainMenu, playerName)
                        Thread.sleep(500)
                        uiThread {
                            progressDoalog.progress = 5
                            progressDoalog.dismiss()
                            startTrackerGG(mode)
                        }
                    } else if (db.checkIfNewDataNeeded(mode, playerName)) {
                        Log.d("TrackerGG", "Data for $mode exists, but is old, updating")

                        scraper.getMaps(mode)
                        uiThread {
                            progressDoalog.progress = 2
                        }
                        scraper.getAgents(mode)
                        uiThread {
                            progressDoalog.progress = 3
                        }
                        scraper.getWeapons(mode)
                        uiThread {
                            progressDoalog.progress = 4
                        }
                        Thread.sleep(500)
                        uiThread {
                            scraper.putToDatabase(mode, this@ValorantMainMenu, playerName)
                            progressDoalog.progress = 5
                            progressDoalog.dismiss()
                            startTrackerGG(mode)
                        }
                    } else {
                        Log.d("TrackerGG", "Data for $mode exists, starting activity")
                        progressDoalog.progress = 5
                        progressDoalog.dismiss()
                        startTrackerGG(mode)
                    }

                } else {
                    uiThread {
                        progressDoalog.dismiss()
                        // show dialog saying player is not signed in at tracker.gg
                        val builder =
                            AlertDialog.Builder(this@ValorantMainMenu, R.style.AlertDialogTheme)
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
                    progressDoalog.dismiss()
                    Toast.makeText(this@ValorantMainMenu, "Error: $e", Toast.LENGTH_LONG).show()
                    Log.d("TrackerGG", "Error: $e")
                }
            }
        }
    }

    private fun checkForTrackerGG(gameName: String, gameTag: String) {
        // show loading dialog
        // show an alert dialog with options to choose which game mode to view stats on
        val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
        builder.setTitle("Choose a game mode for $gameName#$gameTag")
        val gameModes = arrayOf("Competitive", "Unrated", "Spike Rush")
        var mode = ""
        builder.setItems(gameModes) { _, which ->
            when (which) {
                0 -> {
                    // ranked
                    mode = "competitive"
                    loadTrackerGG(gameName, gameTag, mode)
                }
                1 -> {
                    // unrated
                    mode = "unrated"
                    loadTrackerGG(gameName, gameTag, mode)
                }
                2 -> {
                    // competitive
                    mode = "spikerush"
                    loadTrackerGG(gameName, gameTag, mode)
                }
            }
        }
        builder.show()
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

    private fun startTrackerGG(mode: String) {
        val intent = Intent(this, TrackerGG_Activity::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("playerName", playerName)
        startActivity(intent)
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
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
        val playerProfile: ImageView = findViewById(R.id.chatPlayerProfile)
        val playerLevel: TextView = findViewById(R.id.playerLevel)
        doAsync {
            val data =
                Henrik(this@ValorantMainMenu).henrikAPI("https://api.henrikdev.xyz/valorant/v1/account/${RiotName}/${RiotID}?force=true")["data"] as JSONObject
            val largePic = data.getJSONObject("card").getString("large") as String
            val smolPic = data.getJSONObject("card").getString("small") as String
            playerImage = smolPic
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
        val currentTier = "https://api.henrikdev.xyz/valorant/v1/mmr/$region/${RiotName}/$RiotID"

        doAsync {
            try {
                val currentTierData = Henrik(this@ValorantMainMenu).henrikAPI(currentTier)
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
        val allmatches =
            "https://api.henrikdev.xyz/valorant/v3/matches/$region/$RiotName/$RiotID?size=1"
        val agentImageMainMenu: ImageView = findViewById(R.id.agentImageMainMenu)
        doAsync {
            try {
                val lastMatchData = Henrik(this@ValorantMainMenu).henrikAPI(allmatches)
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

                val widgetMatchURL = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"
                val widgetMatchData = Henrik(this@ValorantMainMenu).henrikAPI(widgetMatchURL)

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


                    val matchesDB = MatchDatabase(this@ValorantMainMenu)
                    matchesDB.deleteMatch()
                    matchesDB.insertMatch(matchID, widgetMatchData.toString())

                    val widgetIntent = Intent(this@ValorantMainMenu, LastMatchWidget::class.java)
                    widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                        ComponentName(applicationContext, LastMatchWidget::class.java)
                    )
                    widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                    sendBroadcast(widgetIntent)

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
                seekBar.max = number
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
        matchintent.putExtra("Region", region)
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
                v.animate().alpha(1f).setDuration(500).translationXBy(-500f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.startDelay = delay
            } else {
                v.translationX = -500f
                v.animate().alpha(1f).setDuration(500).translationXBy(500f).setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.startDelay = delay
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

    private fun showLatestFeature(feature: String, description: String, show: Boolean) {
        // check if the user has seen the latest feature
        val sharedPreferences = getSharedPreferences("LatestFeature", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val latestFeature = sharedPreferences.getString("LatestFeature", "0")

        // if the user has not seen the latest feature, show a dialog about it and save that the user has seen it
        if (latestFeature != feature && show) {
            val alertDialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
            alertDialog.setTitle("New feature: $feature!")
            alertDialog.setMessage(description)
            alertDialog.setPositiveButton("Ok") { dialog, which ->
                dialog.dismiss()
            }

            alertDialog.show()

            editor.putString("LatestFeature", feature)
            editor.apply()

            editor.putString("LatestFeatureDescription", description)
            editor.apply()
        }
    }

}
