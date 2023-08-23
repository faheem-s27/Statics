package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.BounceInterpolator
import android.view.animation.DecelerateInterpolator
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.database.FirebaseDatabase
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.activities.MMRActivity
import com.jawaadianinc.valorant_stats.valo.activities.TrackerGGScraper
import com.jawaadianinc.valorant_stats.valo.activities.TrackerGG_Activity
import com.jawaadianinc.valorant_stats.valo.activities.ViewMatches
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.jawaadianinc.valorant_stats.valo.databases.MatchDatabase
import com.jawaadianinc.valorant_stats.valo.databases.TrackerDB
import com.jawaadianinc.valorant_stats.valo.match_info.MatchHistoryActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class StaticsMainMenu : Fragment() {
    lateinit var playerName: String
    lateinit var region: String
    private var puuid: String? = null

    var key: String = ""
    private var lastMatchData: JSONObject? = null
    private lateinit var seekBar: SeekBar
    private var REFRESHING = false
    private var testing = false
    private var JSONRanks = JSONArray()
    private val scraper = TrackerGGScraper()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_statics_main_menu, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //binding = ActivityStaticsMainMenuBinding.inflate(layoutInflater)
        //activity.setSupportActionBar(toolbar)
        //progressDialog.show()

        playerName = activity?.intent?.getStringExtra("playerName") ?: return
        region = activity?.intent?.getStringExtra("region") ?: return
        key = activity?.intent?.getStringExtra("key") ?: return

        val newPlayerBackgroundImage = view.findViewById<ImageView>(R.id.new_playerBackground)
        val newPlayerWideImage = view.findViewById<ImageView>(R.id.new_playerWideImage)

        Picasso.get().load(StaticsMainActivity.playerCardLarge).fit().centerCrop()
            .transform(BlurTransformation(requireContext())).into(newPlayerBackgroundImage)
        Picasso.get().load(StaticsMainActivity.playerCardWide).into(newPlayerWideImage)

        dissapearViews()

        GlobalScope.launch {
            val URL = "https://valorant-api.com/v1/competitivetiers"
            val json = JSONObject(URL(URL).readText()).getJSONArray("data")
            // get last element
            val last = json.getJSONObject(json.length() - 1)
            JSONRanks = last.getJSONArray("tiers")
            withContext(Dispatchers.Main)
            {
                getCurrentSeason()
                if (activity != null) setup()
            }
        }
//
//        doAsync {
//            val URL = "https://valorant-api.com/v1/competitivetiers"
//            val json = JSONObject(URL(URL).readText()).getJSONArray("data")
//            // get last element
//            val last = json.getJSONObject(json.length() - 1)
//            JSONRanks = last.getJSONArray("tiers")
//            uiThread {
//                //testPlayer("BallFondler#His", "eu")
//                getCurrentSeason()
//                setup()
//            }
//        }
    }

    private fun testPlayer(name: String, region: String) {
        testing = true
        playerName = name
        this.region = region
    }

    private fun dissapearViews() {
        val playerCardView: RelativeLayout =
            view?.findViewById(R.id.new_LayoutPartyPlayer) ?: return
        // for each child in the playerCardView, set alpha to 0
        for (i in 0 until playerCardView.childCount) {
            val v = playerCardView.getChildAt(i)
            // animate the alpha to 0
            ObjectAnimator.ofFloat(v, "alpha", 0f).apply {
                duration = 500
                start()
            }
        }

        val mmrCardView: RelativeLayout = view?.findViewById(R.id.new_LayoutMMR)!!
        for (i in 0 until mmrCardView.childCount) {
            val v = mmrCardView.getChildAt(i)
            // animate the alpha to 0
            ObjectAnimator.ofFloat(v, "alpha", 0f).apply {
                duration = 500
                start()
            }
        }

        val matchCardView: RelativeLayout = view?.findViewById(R.id.new_LayoutMatch)!!
        for (i in 0 until matchCardView.childCount) {
            val v = matchCardView.getChildAt(i)
            // animate the alpha to 0
            ObjectAnimator.ofFloat(v, "alpha", 0f).apply {
                duration = 500
                start()
            }
        }
    }

    private fun animateViews() {
        val duration = 1000L
        animation(view?.findViewById(R.id.new_LayoutPartyPlayer)!!, duration, 0)
        animation(view?.findViewById(R.id.new_LayoutMMR)!!, duration, 200)
        animation(view?.findViewById(R.id.new_LayoutMatch)!!, duration, 400)
    }

    private fun animation(layout: RelativeLayout, Duration: Long, Delay: Long) {
        for (i in 0 until layout.childCount) {
            val v = layout.getChildAt(i)
            val animator = ObjectAnimator.ofFloat(v, "translationY", 1000f, 0f)
            animator.duration = Duration
            animator.interpolator = DecelerateInterpolator()
            animator.startDelay = Delay
            animator.start()
            val animator2 = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f)
            animator2.duration = Duration
            animator2.interpolator = DecelerateInterpolator()
            animator2.startDelay = Delay
            animator2.start()
        }
    }

    private fun setup() {
//        //toolbar = (activity as AppCompatActivity).findViewById(R.id.new_mainMenuToolBar2) as MaterialToolbar
//        toolbar.setTitleTextColor(resources.getColor(android.R.color.white))
//        toolbar.title = playerName.split("#")[0]
//        toolbar.subtitle = getString(R.string.loading)
//
//        // inflate menu
//        toolbar.inflateMenu(R.menu.menu_valorant)
//        // listen to menu item clicks
//        toolbar.setOnMenuItemClickListener { item: MenuItem? ->
//            when (item!!.itemId) {
//                R.id.new_refresh -> {
//                    stopTimer()
//                    getLatestDetails()
//                }
//            }
//            true
//        }

        val newPlayerNameText = view?.findViewById<TextView>(R.id.new_partyPlayerName)
        val newPlayerTag = view?.findViewById<TextView>(R.id.new_partyPlayerTag)
        val newPlayerRegion = view?.findViewById<TextView>(R.id.new_playerRegion)

        // if the playername is longer than 14 characters, make the font size smaller for name and tag
        if (playerName.length > 14) {
            newPlayerNameText?.textSize = 12f
            newPlayerTag?.textSize = 12f
        }

        newPlayerNameText?.text = playerName.split("#")[0]
        newPlayerTag?.text = "#" + playerName.split("#")[1]
        // set the regionText to all Caps
        newPlayerRegion?.text = region.uppercase(Locale.ROOT)

        val nameSplit = playerName.split("#")
        if (!testing) {
            puuid = requireActivity().intent.getStringExtra("riotPUUID") ?: return
        //puuid = PlayerDatabase(requireActivity()).getPUUID(nameSplit[0], nameSplit[1])
        }

        seekBar = view?.findViewById(R.id.new_matchesSlider)!!

        val matchButton: FloatingActionButton = view?.findViewById(R.id.new_RecentMatchFAB)!!
        matchButton.setOnClickListener {
            val intent = Intent(requireActivity(), ViewMatches::class.java)
            intent.putExtra("Region", region)
            intent.putExtra("PUUID", puuid)
            intent.putExtra("NumberOfMatches", seekBar.progress.toString())
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        val StatsButton: Button = view?.findViewById(R.id.new_StatsButton)!!
        StatsButton.setOnClickListener {
            // show a dialog to say that these stats are updated once a day
            // only show this dialog once
            val prefs = activity?.getSharedPreferences("trackerGGDialog", Context.MODE_PRIVATE)!!
            val editor = prefs.edit()
            if (!prefs.getBoolean("shown", false)) {  MaterialAlertDialogBuilder(
                    requireActivity(),
                    R.style.AlertDialogTheme
                ).setTitle(getString(R.string.s102))
                    .setMessage(getString(R.string.s101))
                    .setPositiveButton("Ok") { _, _ ->
                        editor.putBoolean("shown", true)
                        editor.apply()
                        checkForTrackerGG(nameSplit[0], nameSplit[1])
                    }
                    .setNegativeButton(getString(R.string.s51)) { _, _ ->
                        //Toast.makeText(requireActivity(), "Cancelled", Toast.LENGTH_SHORT).show()
                    }
                    .setIcon(android.R.drawable.ic_dialog_alert).show()
            } else {
                checkForTrackerGG(nameSplit[0], nameSplit[1])
            }
        }

        //loadFromDatabase(playerName, region)
        try {
            getLatestDetails()
        } catch (e: Exception) {
            Log.d("newMainMenu", "Error: ${e.message}")
        }
    }

    private fun getLatestDetails() {
        // split the player name into two parts by # and set it to RiotName and RiotID
        val playerNameSplit = playerName.split("#".toRegex()).toTypedArray()
        val riotName = playerNameSplit[0]
        val riotID = playerNameSplit[1]
        val allmatches =
            "https://api.henrikdev.xyz/valorant/v3/matches/$region/$riotName/$riotID?size=1"
        val ranksURL = "https://api.henrikdev.xyz/valorant/v2/mmr/$region/$riotName/$riotID"

        dissapearViews()

        doAsync {
            if (context == null) return@doAsync
            val lastMatchData = Henrik(requireContext()).henrikAPI(allmatches)
            val ranksData = Henrik(requireContext()).henrikAPI(ranksURL)

            uiThread {
                // check if the status code is 200
                if (lastMatchData.getInt("status") == 200 && ranksData.getInt("status") == 200) {
                    loadRankDetails(ranksData)
                    processPlayerDetails(lastMatchData)
                    //updateTimer()
                    animateViews()
                    REFRESHING = false
                } else {
                    // show alert dialog
                    if (context == null) return@uiThread
                    val builder = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
                    builder.setTitle("Error")
                    builder.setMessage(
                        "Statics was not able to get your stats data due to an error!\nError Code: ${
                            lastMatchData.getInt(
                                "status"
                            )
                        } ${ranksData.getInt("status")}, it should be fixed in a couple hours."
                    )
                    builder.setPositiveButton("OK") { dialog, which ->
                        // finish the fragment
                        //requireActivity().finish()
                    }
                    builder.show()

                    // send this error to the firebase database
                    val error = hashMapOf(
                        "errorCode" to "Error Code: ${lastMatchData.getInt("status")} ${
                            ranksData.getInt(
                                "status"
                            )
                        }",
                        "errorMessage" to "JSON: $lastMatchData \n $ranksData",
                        "playerName" to playerName,
                        "region" to region,
                        "time" to convertTime(System.currentTimeMillis()),
                        "url" to allmatches + "\n" + ranksURL
                    )
                    FirebaseDatabase.getInstance().reference.child("Statics/Errors/")
                        .child("NewMainMenu").push().setValue(error)

                    //progressDialog.dismiss()
                }
            }
        }
    }

    private fun convertTime(time: Long): String {
        val date = Date(time)
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm:ss")
        return format.format(date)
    }

    private fun processPlayerDetails(matchData: JSONObject) {
        val matchDataArray: JSONObject
        // hashmap of player name and score
        val playerScore = HashMap<String, Int>()
        if (context == null) return
        // check if the size of the data array is 0 and tell the user to play a match
        if (matchData.getJSONArray("data").length() == 0) {
            val smol =
                "https://media.valorant-api.com/playercards/9fb348bc-41a0-91ad-8a3e-818035c4e561/wideart.png"
            val large =
                "https://media.valorant-api.com/playercards/9fb348bc-41a0-91ad-8a3e-818035c4e561/largeart.png"
            val newPlayerBackgroundImage = view?.findViewById<ImageView>(R.id.new_playerBackground)
            val newPlayerWideImage = view?.findViewById<ImageView>(R.id.new_playerWideImage)
            Picasso.get().load(large).fit().centerCrop()
                .transform(BlurTransformation(requireContext())).into(newPlayerBackgroundImage)
            Picasso.get().load(smol).fit().centerCrop()
                .into(newPlayerWideImage)

            val builder = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            builder.setTitle("No Matches Found")
            builder.setMessage("You have not played any matches in a long time so Statics is unable to process your stats. Please play a match and try again.")
            builder.setPositiveButton("OK") { dialog, which ->
            }
            builder.show()

            return
        }

        updateWidget(matchData)

        try {
            matchDataArray = matchData.getJSONArray("data").getJSONObject(0)
            val playersArray = matchDataArray.getJSONObject("players").getJSONArray("all_players")
            // iterate until we find the player we are looking for
            for (i in 0 until playersArray.length()) {
                val currentPlayer = playersArray.getJSONObject(i)
                // add the player name and score to the hashmap
                playerScore[currentPlayer.getString("name")] =
                    currentPlayer.getJSONObject("stats").getInt("score")
                if (currentPlayer.getString("name") == playerName.split("#")[0] && currentPlayer.getString(
                        "tag"
                    ) == playerName.split("#")[1]
                ) {
                    lastMatchData = matchDataArray
                    loadMatchDetails(matchDataArray)
                    loadPlayerDetails(currentPlayer)
                }
            }
        } catch (e: Exception) {
            Log.d("newMainMenu", "Error from playerDetails: ${e.message}")
            // if there is an error, show the error message
            if (context == null) return
            Toast.makeText(
                requireContext(),
                "Error happened while loading player data",
                Toast.LENGTH_LONG
            )
                .show()

        }

        // sort the hashmap by the score
        val sortedPlayerScore =
            playerScore.toList().sortedByDescending { (_, value) -> value }.toMap()

        val newPlayerPositionText = view?.findViewById<TextView>(R.id.new_playerPosition)

        // if our position is 1, then add "st" to the end of the position
        when (val ourPosition = sortedPlayerScore.keys.indexOf(playerName.split("#")[0]) + 1) {
            1 -> {
                newPlayerPositionText?.text = "${ourPosition}st"
                // make the text colour gold if we are first
                newPlayerPositionText?.setTextColor(Color.parseColor("#FFD700"))
            }
            // if our position is 2, then add "nd" to the end of the position
            2 -> {
                newPlayerPositionText?.text = "${ourPosition}nd"
                // make the text colour silver if we are second
                newPlayerPositionText?.setTextColor(Color.parseColor("#C0C0C0"))
            }
            // if our position is 3, then add "rd" to the end of the position
            3 -> {
                newPlayerPositionText?.text = "${ourPosition}rd"
                // make the text colour bronze if we are third
                newPlayerPositionText?.setTextColor(Color.parseColor("#CD7F32"))
            }
            // if our position is 4 or more, then add "th" to the end of the position
            else -> {
                newPlayerPositionText?.text = "${ourPosition}th"
                // make the text colour white if we are fourth or lower
                newPlayerPositionText?.setTextColor(Color.parseColor("#FFFFFF"))
            }
        }
    }

    private fun updateWidget(matchData: JSONObject) {
        val matchesDB = MatchDatabase(requireActivity())
        matchesDB.deleteMatch()
        matchesDB.insertMatch(
            matchData.getJSONArray("data").getJSONObject(0).getJSONObject("metadata")
                .getString("matchid"),
            matchData.toString()
        )
        val widgetIntent = Intent(requireActivity(), LastMatchWidget::class.java)
        widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(requireActivity().application).getAppWidgetIds(
            ComponentName(requireActivity().applicationContext, LastMatchWidget::class.java)
        )
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        requireActivity().sendBroadcast(widgetIntent)
    }

    private fun loadPlayerDetails(currentPlayer: JSONObject) {
        if (context == null) return
        val db = AssetsDatabase(requireContext())
        val title = db.retrieveName(currentPlayer.getString("player_title"))
        val image = currentPlayer.getJSONObject("assets").getJSONObject("card").getString("large")
        val wideImage =
            currentPlayer.getJSONObject("assets").getJSONObject("card").getString("wide")
        val smallImage =
            currentPlayer.getJSONObject("assets").getJSONObject("card").getString("small")
        val newPlayerTitleText = view?.findViewById<TextView>(R.id.new_playerTitle)
        val newPlayerLevelText = view?.findViewById<TextView>(R.id.new_playerLevel)
        val newPlayerBackgroundImage = view?.findViewById<ImageView>(R.id.new_playerBackground)
        val newPlayerWideImage = view?.findViewById<ImageView>(R.id.new_playerWideImage)
        newPlayerTitleText?.text = title
        newPlayerLevelText?.text = currentPlayer.getString("level")
        // Picasso and blur the image
        Picasso.get().load(image).fit().centerCrop()
            .transform(BlurTransformation(requireContext())).into(newPlayerBackgroundImage)
        Picasso.get().load(wideImage).fit().centerCrop().into(newPlayerWideImage)
    }


    private fun loadRankDetails(rankHistory: JSONObject) {
        val newPlayerRankTimePlayedText =
            view?.findViewById<TextView>(R.id.new_playerRankTimePlayed)
        val newRankProgressBar =
            view?.findViewById<CircularProgressIndicator>(R.id.new_rankProgressBar)
        val newPlayerChangeRRText = view?.findViewById<TextView>(R.id.new_playerChangeRR)
        val newPlayerRRText = view?.findViewById<TextView>(R.id.new_playerRRText)
        val newPlayerRankTitleText = view?.findViewById<TextView>(R.id.new_playerRankTitle)
        val newPlayerRankImage = view?.findViewById<ImageView>(R.id.new_playerRankImage)
        val newPlayerPastsRanks = view?.findViewById<MaterialButton>(R.id.new_playerPastRanks)

        val newPeakRankText = view?.findViewById<TextView>(R.id.new_peakplayerRankTitle)
        val newPeakSeason = view?.findViewById<TextView>(R.id.new_peakSeason)
        val newPeakRankImage = view?.findViewById<ImageView>(R.id.new_peakplayerRankImage)

        try {
            val rankData = rankHistory.getJSONObject("data").getJSONObject("current_data")
            val title = rankData.getString("currenttierpatched")
            val progress = rankData.getString("ranking_in_tier")
            val change = rankData.getString("mmr_change_to_last_game")
            //val dateRaw = rankData.getString("date_raw")
            //newPlayerRankTimePlayedText?.text = "Comped ${timeAgo(dateRaw.toLong())}"

            // if change is positive, then the progress bar colour is Valorant blue, else it is red
            if (change.toInt() > 0) {
                newRankProgressBar?.setIndicatorColor(resources.getColor(R.color.Valorant_Blue))
                newPlayerChangeRRText?.text = "+$change"
                newPlayerChangeRRText?.setTextColor(resources.getColor(R.color.Valorant_Blue))
                // change stroke colour to blue
                newPlayerPastsRanks?.strokeColor =
                    resources.getColorStateList(R.color.Valorant_Blue)

            } else {
                newRankProgressBar?.setIndicatorColor(resources.getColor(R.color.Valorant_Red))
                newPlayerChangeRRText?.text = change
                newPlayerChangeRRText?.setTextColor(resources.getColor(R.color.Valorant_Red))
                // change stroke colour to red
                newPlayerPastsRanks?.strokeColor =
                    resources.getColorStateList(R.color.Valorant_Red)
            }

            newPlayerRRText?.text = "$progress/100"
            animateRankChanges(progress.toInt())
            newPlayerRankTitleText?.text = title
            val rankImage =
                rankData.getJSONObject("images").getString("large")
            Picasso.get().load(rankImage).fit().centerInside().into(newPlayerRankImage)

            newPlayerPastsRanks?.setOnClickListener {
                val name1 = playerName.split("#")
                val intent = Intent(requireContext(), MMRActivity::class.java)
                intent.putExtra("RiotName", name1[0])
                intent.putExtra("RiotID", name1[1])
                intent.putExtra("key", key)
                startActivity(intent)
                activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }

            val peakRankData = rankHistory.getJSONObject("data").getJSONObject("highest_rank")
            val peakTitle = peakRankData.getString("patched_tier")
            newPeakRankText?.text = peakTitle
            val peakSeason = peakRankData.getString("season")
            newPeakSeason?.text = formatSeasonPeakName(peakSeason)
            Picasso.get().load(findPeakRankImage(peakTitle)).fit().centerInside()
                .into(newPeakRankImage)

        } catch (e: Exception) {
            Log.d("newMainMenu", "Error for rank: ${e.message}")
            try {
                Picasso.get()
                    .load("https://media.valorant-api.com/competitivetiers/564d8e28-c226-3180-6285-e48a390db8b1/0/smallicon.png")
                    .fit().centerInside().into(newPlayerRankImage)
                newPlayerRankTitleText?.text = "Unranked"
                newRankProgressBar?.progress = 0
                newPlayerRRText?.text = "0/100"
            } catch (e: Exception) {
                Log.d("newMainMenu", "Error for rank: ${e.message}")
            }
        }
    }

    private fun findPeakRankImage(RankName: String): String {
        // Capitalise the entire string
        val rankName = RankName.uppercase(Locale.ROOT)
        for (i in 0 until JSONRanks.length()) {
            if (JSONRanks.getJSONObject(i).getString("tierName") == rankName) {
                return JSONRanks.getJSONObject(i).getString("largeIcon")
            }
        }
        return "https://media.valorant-api.com/competitivetiers/564d8e28-c226-3180-6285-e48a390db8b1/0/smallicon.png"
    }

    private fun formatSeasonPeakName(season: String): String {
        // its formatted as "e5a1" where E is the Episode and A is the Act so return "Episode 5 Act 1"
        val episode = season[1].toString().toInt()
        val act = season[3].toString().toInt()
        return "ACT $act - EPISODE $episode"
    }

    private fun timeAgo(unixTime: Long): String {
        val time = unixTime * 1000
        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            // get the time left until the future date
            val diff = time - now
            val seconds = diff / 1000
            val minutes = seconds / 60
            val hours = minutes / 60
            val days = hours / 24
            val weeks = days / 7
            val months = days / 30
            val years = days / 365
            return when {
                seconds < 60 -> {
                    "Just now"
                }
                minutes < 60 -> {
                    "$minutes minutes left"
                }
                hours < 24 -> {
                    "$hours hours left"
                }
                days < 7 -> {
                    "$days days left"
                }
                weeks < 4 -> {
                    "$weeks weeks left"
                }
                months < 12 -> {
                    "$months months left"
                }
                else -> {
                    "$years years left"
                }
            }
        }
        val diff = now - time
        val MINUTE_MILLIS = 60 * 1000
        val HOUR_MILLIS = 60 * MINUTE_MILLIS
        val DAY_MILLIS = 24 * HOUR_MILLIS
        return if (diff < MINUTE_MILLIS) {
            "Just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            (diff / MINUTE_MILLIS).toString() + " minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            (diff / HOUR_MILLIS).toString() + " hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            "Yesterday"
        } else {
            (diff / DAY_MILLIS).toString() + " ${getString(R.string.s176)}"
        }
    }

    private fun animateRankChanges(maxValue: Int) {
        val duration = 3000
        val progressBar = view?.findViewById<CircularProgressIndicator>(R.id.new_rankProgressBar)
        val progressAnimator =
            ObjectAnimator.ofInt(progressBar, "progress", 0, maxValue)
        progressAnimator.duration = duration.toLong()
        progressAnimator.interpolator = BounceInterpolator()
        progressAnimator.startDelay = 1000
        progressAnimator.start()
    }

    private fun getCurrentSeason() {
        val URL = "https://valorant-api.com/v1/seasons"

        val newCurrentSeasonText = view?.findViewById<TextView>(R.id.new_currentSeason)
        val newCurrentSeasonEndingText = view?.findViewById<TextView>(R.id.new_currentSeasonEnding)

        //Log.d("newMainMenu", "Getting current season")

        doAsync {
            val seasonsJSON = JSONObject(URL(URL).readText()).getJSONArray("data")
            // go to last index which has parentUUID not null
            var currentSeason = seasonsJSON.getJSONObject(seasonsJSON.length() - 1)
            val type = currentSeason.getString("type")
            if (type != "EAresSeasonType::Act") {
                currentSeason = seasonsJSON.getJSONObject(seasonsJSON.length() - 2)
            }
            val seasonName = currentSeason.getString("displayName")
            val seasonEnd = currentSeason.getString("endTime")
            val parentUUID = currentSeason.getString("parentUuid")

            // Log.d("newMainMenu", "Current season is $seasonName")

            // find the parent season
            for (i in 0 until seasonsJSON.length()) {
                val season = seasonsJSON.getJSONObject(i)
                if (season.getString("uuid") == parentUUID) {
                    val parentName = season.getString("displayName")
                    uiThread {
                        val seasonName = "$seasonName - $parentName"
                        newCurrentSeasonText?.text = seasonName
                        // parse the seasonEnd date
                        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(seasonEnd)
                        val formatter = SimpleDateFormat("dd MMM yyyy")
                        val formattedDate = date?.let { it1 -> formatter.format(it1) }
                        // convert formatter date to unix time
                        val unixTime = date?.time?.div(1000)
                        val timeLeft = timeAgo(unixTime!!)
                        newCurrentSeasonEndingText?.text = "$formattedDate\n($timeLeft)"

                        Log.d(
                            "newMainMenu",
                            "Current season is $seasonName, ending on $formattedDate ($timeLeft)"
                        )
                    }
                    break
                }
            }
        }
    }

//    private fun updateTimer() {
//        // only one timer is needed
//        if (timer != null) {
//            timer!!.cancel()
//        }
//
//        progressDialog.dismiss()
//
//        timer = object : CountDownTimer(60000, 1000) {
//            override fun onTick(millisUntilFinished: Long) {
//                if (isAdded) {
//                    if (ISACTIVE) toolbar.subtitle =
//                        "${getString(R.string.s20)} ${millisUntilFinished / 1000}"
//                    else {
//                        timer?.cancel()
//                    }
//                }
//            }
//
//            override fun onFinish() {
//                if (isAdded) {
//                    if (ISACTIVE) {
//                        toolbar.subtitle = getString(R.string.s100)
//                        getLatestDetails()
//                    } else {
//                        timer?.cancel()
//                    }
//                }
//            }
//        }
//        (timer as CountDownTimer).start()
//    }
//
//    private fun stopTimer() {
//        REFRESHING = true
//        timer?.cancel()
//        if (ISACTIVE) toolbar.subtitle = getString(R.string.s99)
//    }

    private fun loadMatchDetails(lastMatchData: JSONObject) {

        val newMatchMapNameText = view?.findViewById<TextView>(R.id.new_matchMapName)
        val newMapMatchImage = view?.findViewById<ImageView>(R.id.new_matchMapImage)
        val newMatchGameModeText = view?.findViewById<TextView>(R.id.new_matchGameMode)
        val newMatchRegionText = view?.findViewById<TextView>(R.id.new_matchRegion)
        val newMatchMapDate = view?.findViewById<TextView>(R.id.new_matchMapDate)
        val newMatchKDAText = view?.findViewById<TextView>(R.id.new_matchKDA)
        if (context == null) return
        val db = AssetsDatabase(requireContext())

        val metaData = lastMatchData.getJSONObject("metadata")
        val matchID = metaData.getString("matchid")
        newMatchMapNameText?.text = metaData.getString("map")

        val mapImage = db.retrieveImage(metaData.getString("map"))
        val blurred = myblur(mapImage, requireContext())
        newMapMatchImage?.setImageBitmap(blurred)

        newMapMatchImage?.setOnClickListener {
            matchActivityStart(playerName.split("#")[0], playerName.split("#")[1], matchID)
        }

        newMatchGameModeText?.text = metaData.getString("mode")
        newMatchRegionText?.text = metaData.getString("cluster")

        val timePlayed = timeAgo(metaData.getLong("game_start"))
        newMatchMapDate?.text = timePlayed

        val playerData = lastMatchData.getJSONObject("players")
        newMatchKDAText?.text = getKDA(playerData.getJSONArray("all_players"))

        seekBarMatches(key)
    }

    private fun getKDA(playerJSON: JSONArray): String {
        // loop through the player array until playername is found
        val newMatchAgentImage = view?.findViewById<ImageView>(R.id.new_matchAgentImage)

        for (i in 0 until playerJSON.length()) {
            val player = playerJSON.getJSONObject(i)
            if (player.getString("name") + "#" + player.getString("tag") == playerName) {
                val kills = player.getJSONObject("stats").getInt("kills")
                val deaths = player.getJSONObject("stats").getInt("deaths")
                val assists = player.getJSONObject("stats").getInt("assists")

                val agentImage =
                    player.getJSONObject("assets").getJSONObject("agent").getString("small")
                Picasso.get().load(agentImage).into(newMatchAgentImage)
                processRoundNumbers(player.getString("team").lowercase(Locale.ROOT))
                return "$kills/$deaths/$assists"
            }
        }
        return "0/0/0"
    }

    private fun processRoundNumbers(team: String) {
        val teams = lastMatchData?.getJSONObject("teams")

        val newMatchAllyScore = view?.findViewById<TextView>(R.id.new_matchAllyScore)
        val newMatchEnemyScore = view?.findViewById<TextView>(R.id.new_matchEnemyScore)

        if (team == "red") {
            val teamScore = teams?.getJSONObject("red")?.getInt("rounds_won")
            newMatchAllyScore?.text = teamScore.toString()
            newMatchEnemyScore?.text =
                teams?.getJSONObject("blue")?.getInt("rounds_won").toString()

            // make ally score valorant red and enemy score valorant blue
            newMatchAllyScore?.setTextColor(Color.parseColor("#f94555"))
            newMatchEnemyScore?.setTextColor(Color.parseColor("#18e4b7"))
        } else if (team == "blue") {
            val teamScore = teams?.getJSONObject("blue")?.getInt("rounds_won")
            newMatchAllyScore?.text = teamScore.toString()
            newMatchEnemyScore?.text =
                teams?.getJSONObject("red")?.getInt("rounds_won").toString()

            // make ally score valorant blue and enemy score valorant red
            newMatchAllyScore?.setTextColor(Color.parseColor("#18e4b7"))
            newMatchEnemyScore?.setTextColor(Color.parseColor("#f94555"))
        }
    }

    private fun myblur(image: Bitmap, context: Context?): Bitmap? {
        val BITMAP_SCALE = 1f
        val BLUR_RADIUS = 1f
        val width = (image.width * BITMAP_SCALE).roundToInt()
        val height = (image.height * BITMAP_SCALE).roundToInt()
        val inputBitmap = Bitmap.createScaledBitmap(image, width, height, false)
        val outputBitmap = Bitmap.createBitmap(inputBitmap)
        val rs = RenderScript.create(context)
        val theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs))
        val tmpIn = Allocation.createFromBitmap(rs, inputBitmap)
        val tmpOut = Allocation.createFromBitmap(rs, outputBitmap)
        theIntrinsic.setRadius(BLUR_RADIUS)
        theIntrinsic.setInput(tmpIn)
        theIntrinsic.forEach(tmpOut)
        tmpOut.copyTo(outputBitmap)
        return outputBitmap
    }

    private fun seekBarMatches(key: String) {
        val riotPUUID = requireActivity().intent.getStringExtra("riotPUUID")
        val url =
            "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${riotPUUID}?api_key=${key}"

        doAsync {
            val number = JSONObject(URL(url).readText()).getJSONArray("history").length()
            uiThread {
                seekBar.max = number
                if (number > 0) {
                    seekBar.progress = number - 1
                }
            }
        }

        val howManyMatches: TextView = view?.findViewById(R.id.new_matchSliderNumber)!!
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                howManyMatches.text = "$progress"
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
            }
        })
    }

    private fun matchActivityStart(Name: String, ID: String, matchID: String) {
        val matchintent = Intent(requireActivity(), MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", 0)
        matchintent.putExtra("MatchID", matchID)
        matchintent.putExtra("Region", region)
        startActivity(matchintent)
    }

    private fun checkForTrackerGG(gameName: String, gameTag: String) {
        // show loading dialog
        // show an alert dialog with options to choose which game mode to view stats on
        val builder = MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogTheme)
        builder.setTitle("${getString(R.string.s216)} $gameName#$gameTag")
        val gameModes =
            arrayOf(getString(R.string.s149), getString(R.string.s148), getString(R.string.s147))
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

    private fun loadTrackerGG(gameName: String, gameTag: String, mode: String) {
        val progressDoalog = ProgressDialog(requireActivity())
        progressDoalog.max = 5
        progressDoalog.setMessage(getString(R.string.s217))
        progressDoalog.setTitle("$gameName#$gameTag's $mode stats")
        progressDoalog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        progressDoalog.show()

        val db = TrackerDB(requireActivity())

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

                        scraper.putToDatabase(mode, requireActivity(), playerName)
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
                            scraper.putToDatabase(mode, requireActivity(), playerName)
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
                        val builder = MaterialAlertDialogBuilder(
                                requireActivity(),
                                R.style.AlertDialogTheme
                            )
                        builder.setTitle("Profile is private")
                        builder.setMessage("To continue, you need to be signed in at tracker.gg and have your profile set to public.")
                        builder.setPositiveButton("Sign in") { _, _ ->
                            signIntoTrackerGG(gameName, gameTag)
                        }
                        builder.setNegativeButton("Cancel") { _, _ ->
                            Toast.makeText(
                                requireActivity(),
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
                    Toast.makeText(requireActivity(), "Error: $e", Toast.LENGTH_LONG).show()
                    Log.d("TrackerGG", "Error: $e")
                }
            }
        }
    }

    private fun startTrackerGG(mode: String) {
        val intent = Intent(requireActivity(), TrackerGG_Activity::class.java)
        intent.putExtra("mode", mode)
        intent.putExtra("playerName", playerName)
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
    }

    private fun signIntoTrackerGG(name: String, tag: String) {
        val signInURL = "https://tracker.gg/valorant/profile/riot/$name%23$tag/overview"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(signInURL)
        startActivity(intent)
    }

}
