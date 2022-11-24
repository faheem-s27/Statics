package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.os.CountDownTimer
import android.renderscript.Allocation
import android.renderscript.Element
import android.renderscript.RenderScript
import android.renderscript.ScriptIntrinsicBlur
import android.util.Log
import android.view.MenuItem
import android.view.animation.DecelerateInterpolator
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.FirebaseDatabase
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityStaticsMainMenuBinding
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.activities.MMRActivity
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


class StaticsMainMenu : AppCompatActivity() {
    lateinit var playerName: String
    lateinit var region: String
    var key: String = ""
    lateinit var binding: ActivityStaticsMainMenuBinding
    var toolbar: MaterialToolbar? = null
    private var timer: CountDownTimer? = null
    private var lastMatchData: JSONObject? = null

    private var REFRESHING = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the player name from the previous activity
        playerName = intent.getStringExtra("playerName").toString()
        region = intent.getStringExtra("region").toString()
        key = intent.getStringExtra("key").toString()
        setup()
    }

    private fun dissapearViews() {
        val playerCardView: RelativeLayout = findViewById(R.id.new_LayoutPlayer)
        // for each child in the playerCardView, set alpha to 0
        for (i in 0 until playerCardView.childCount) {
            val v = playerCardView.getChildAt(i)
            // animate the alpha to 0
            ObjectAnimator.ofFloat(v, "alpha", 0f).apply {
                duration = 500
                start()
            }

        }

        val mmrCardView: RelativeLayout = findViewById(R.id.new_LayoutMMR)
        for (i in 0 until mmrCardView.childCount) {
            val v = mmrCardView.getChildAt(i)
            // animate the alpha to 0
            ObjectAnimator.ofFloat(v, "alpha", 0f).apply {
                duration = 500
                start()
            }
        }

        val matchCardView: RelativeLayout = findViewById(R.id.new_LayoutMatch)
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
        val Duration = 1000L
        animation(binding.newLayoutPlayer, Duration, 0)
        animation(binding.newLayoutMMR, Duration, 200)
        animation(binding.newLayoutMatch, Duration, 400)

//        for (i in 0 until playerCardView.childCount) {
//            // start from bottom and then accelerate to top, with alpha 0 to 1
//            val v = playerCardView.getChildAt(i)
//            val animator = ObjectAnimator.ofFloat(v, "translationY", 1000f, 0f)
//            animator.duration = Duration
//            animator.interpolator = DecelerateInterpolator()
//            animator.start()
//            val animator2 = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f)
//            animator2.duration = Duration
//            animator2.interpolator = DecelerateInterpolator()
//            animator2.start()
//
//            // move them down
//            ObjectAnimator.ofFloat(v, "translationY", 1000f, 0f).apply {
//                duration = Duration
//                interpolator = DecelerateInterpolator()
//                start()
//            }
//        }
//
//        val mmrCardView : RelativeLayout = findViewById(R.id.new_LayoutMMR)
//        for (i in 0 until mmrCardView.childCount) {
//            val v = mmrCardView.getChildAt(i)
//            val animator = ObjectAnimator.ofFloat(v, "translationY", 1000f, 0f)
//            animator.duration = Duration
//            animator.interpolator = DecelerateInterpolator()
//            animator.startDelay = 100
//            animator.start()
//            val animator2 = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f)
//            animator2.duration = Duration
//            animator2.interpolator = DecelerateInterpolator()
//            animator2.startDelay = 100
//            animator2.start()
//        }
//
//        val matchCardView : RelativeLayout = findViewById(R.id.new_LayoutMatch)
//        for (i in 0 until matchCardView.childCount) {
//            val v = matchCardView.getChildAt(i)
//            val animator = ObjectAnimator.ofFloat(v, "translationY", 1000f, 0f)
//            animator.duration = Duration
//            animator.interpolator = DecelerateInterpolator()
//            animator.startDelay = 200
//            animator.start()
//            val animator2 = ObjectAnimator.ofFloat(v, "alpha", 0f, 1f)
//            animator2.duration = Duration
//            animator2.interpolator = DecelerateInterpolator()
//            animator2.startDelay = 200
//            animator2.start()
//        }
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
        toolbar = binding.newMainMenuToolBar
        toolbar!!.setTitleTextColor(resources.getColor(android.R.color.white))
        toolbar!!.title = playerName.split("#")[0]
        toolbar!!.subtitle = "Loading..."

        // inflate menu
        toolbar!!.inflateMenu(R.menu.menu_valorant)
        // listen to menu item clicks
        toolbar!!.setOnMenuItemClickListener { item: MenuItem? ->
            when (item!!.itemId) {
                R.id.new_refresh -> {
                    stopTimer()
                    getLatestDetails()
                }
            }
            true
        }

        binding.newPlayerName.text = playerName.split("#")[0]
        binding.newPlayerTag.text = "#" + playerName.split("#")[1]
        // set the regionText to all Caps
        binding.newPlayerRegion.text = region.uppercase(Locale.ROOT)

        Log.d("newMainMenu", "playerName: $playerName, region: $region")

        loadFromDatabase(playerName, region)

    }

    private fun loadFromDatabase(playerName: String, region: String) {
        //TODO load from database and then update the UI with the data


        // Gets latest data from the API
        getLatestDetails()
    }

    private fun getLatestDetails() {
        // split the player name into two parts by # and set it to RiotName and RiotID
        val playerNameSplit = playerName.split("#".toRegex()).toTypedArray()
        val riotName = playerNameSplit[0]
        val riotID = playerNameSplit[1]
        val allmatches =
            "https://api.henrikdev.xyz/valorant/v3/matches/$region/$riotName/$riotID?size=1"
        val ranksURL = "https://api.henrikdev.xyz/valorant/v1/mmr-history/$region/$riotName/$riotID"

        dissapearViews()
        getCurrentSeason()

        doAsync {
            val lastMatchData = Henrik(this@StaticsMainMenu).henrikAPI(allmatches)
            val ranksData = Henrik(this@StaticsMainMenu).henrikAPI(ranksURL)
            uiThread {
                // check if the status code is 200
                if (lastMatchData.getInt("status") == 200) {
                    loadRankDetails(ranksData)
                    processPlayerDetails(lastMatchData)
                    updateTimer()
                    animateViews()
                    REFRESHING = false
                } else {
                    // show alert dialog
                    val builder = AlertDialog.Builder(this@StaticsMainMenu)
                    builder.setTitle("Error")
                    builder.setMessage(
                        "Unable to fetch data. Please try again later.\nError Code: ${
                            lastMatchData.getInt(
                                "status"
                            )
                        }, please report this to the developer."
                    )
                    builder.setPositiveButton("OK") { dialog, which ->
                        // finish the activity
                        finish()
                    }
                    val dialog: AlertDialog = builder.create()
                    dialog.show()

                    // send this error to the firebase database
                    val error = hashMapOf(
                        "errorCode" to "Error Code: ${lastMatchData.getInt("status")}",
                        "errorMessage" to "JSON: $lastMatchData",
                        "playerName" to playerName,
                        "region" to region,
                        "key" to key
                    )
                    FirebaseDatabase.getInstance().reference.child("Statics/Errors/")
                        .child("NewMainMenu").push().setValue(error)
                }
            }
        }
    }

    private fun processPlayerDetails(matchData: JSONObject) {
        val matchDataArray: JSONObject
        // hashmap of player name and score
        val playerScore = HashMap<String, Int>()

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
            Toast.makeText(this, "Error happened while loading player data", Toast.LENGTH_LONG)
                .show()

        }

        // sort the hashmap by the score
        val sortedPlayerScore =
            playerScore.toList().sortedByDescending { (_, value) -> value }.toMap()

        // if our position is 1, then add "st" to the end of the position
        when (val ourPosition = sortedPlayerScore.keys.indexOf(playerName.split("#")[0]) + 1) {
            1 -> {
                binding.newPlayerPosition.text = "${ourPosition}st"
            }
            // if our position is 2, then add "nd" to the end of the position
            2 -> {
                binding.newPlayerPosition.text = "${ourPosition}nd"
            }
            // if our position is 3, then add "rd" to the end of the position
            3 -> {
                binding.newPlayerPosition.text = "${ourPosition}rd"
            }
            // if our position is 4 or more, then add "th" to the end of the position
            else -> {
                binding.newPlayerPosition.text = "${ourPosition}th"
            }
        }
    }

    private fun loadPlayerDetails(currentPlayer: JSONObject) {
        val db = AssetsDatabase(this)
        val title = db.retrieveName(currentPlayer.getString("player_title"))
        val image = currentPlayer.getJSONObject("assets").getJSONObject("card").getString("large")
        val wideImage =
            currentPlayer.getJSONObject("assets").getJSONObject("card").getString("wide")
        binding.newPlayerTitle.text = title
        binding.newPlayerLevel.text = currentPlayer.getString("level")
        // Picasso and blur the image
        Picasso.get().load(image).fit().centerCrop()
            .transform(BlurTransformation(this@StaticsMainMenu)).into(binding.newPlayerBackground)
        Picasso.get().load(wideImage).fit().centerCrop().into(binding.newPlayerWideImage)
    }

    private fun loadRankDetails(rankHistory: JSONObject) {
        try {
            val rankData = rankHistory.getJSONArray("data").getJSONObject(0)
            val title = rankData.getString("currenttierpatched")
            val progress = rankData.getString("ranking_in_tier")
            val change = rankData.getString("mmr_change_to_last_game")
            val dateRaw = rankData.getString("date_raw")

            binding.newPlayerRankTimePlayed.text = "Comped ${timeAgo(dateRaw.toLong())}"

            // if change is positive, then the progress bar colour is Valorant blue, else it is red
            if (change.toInt() > 0) {
                binding.newRankProgressBar.setIndicatorColor(resources.getColor(R.color.Valorant_Blue))
                binding.newPlayerChangeRR.text = "+$change"
                binding.newPlayerChangeRR.setTextColor(resources.getColor(R.color.Valorant_Blue))

            } else {
                binding.newRankProgressBar.setIndicatorColor(resources.getColor(R.color.Valorant_Red))
                binding.newPlayerChangeRR.text = change
                binding.newPlayerChangeRR.setTextColor(resources.getColor(R.color.Valorant_Red))
            }

            binding.newPlayerRRText.text = "$progress/100"
            animateRankChanges(progress.toInt())
            binding.newPlayerRankTitle.text = title
            val rankImage =
                rankData.getJSONObject("images").getString("large")
            Picasso.get().load(rankImage).fit().centerInside().into(binding.newPlayerRankImage)

            binding.newPlayerPastRanks.setOnClickListener {
                val name1 = playerName.split("#")
                val intent = Intent(this, MMRActivity::class.java)
                intent.putExtra("RiotName", name1[0])
                intent.putExtra("RiotID", name1[1])
                intent.putExtra("key", key)
                startActivity(intent)
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }

        } catch (e: Exception) {
            Log.d("newMainMenu", "Error for rank: ${e.message}")
            Picasso.get()
                .load("https://media.valorant-api.com/competitivetiers/564d8e28-c226-3180-6285-e48a390db8b1/0/smallicon.png")
                .fit().centerInside().into(binding.newPlayerRankImage)
            binding.newPlayerRankTitle.text = "Unranked"
            binding.newRankProgressBar.progress = 0
            binding.newPlayerRRText.text = "0/100"
        }
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
            "just now"
        } else if (diff < 2 * MINUTE_MILLIS) {
            "a minute ago"
        } else if (diff < 50 * MINUTE_MILLIS) {
            (diff / MINUTE_MILLIS).toString() + " minutes ago"
        } else if (diff < 90 * MINUTE_MILLIS) {
            "an hour ago"
        } else if (diff < 24 * HOUR_MILLIS) {
            (diff / HOUR_MILLIS).toString() + " hours ago"
        } else if (diff < 48 * HOUR_MILLIS) {
            "yesterday"
        } else {
            (diff / DAY_MILLIS).toString() + " days ago"
        }
    }

    private fun animateRankChanges(maxValue: Int) {
        val duration = 3000
        val progressBar = binding.newRankProgressBar
        val progressAnimator =
            ObjectAnimator.ofInt(progressBar, "progress", progressBar.progress, maxValue)
        progressAnimator.duration = duration.toLong()
        progressAnimator.interpolator = DecelerateInterpolator()
        progressAnimator.startDelay = 1000
        progressAnimator.start()
    }

    private fun getCurrentSeason() {
        val URL = "https://valorant-api.com/v1/seasons"
        doAsync {
            val seasonsJSON = JSONObject(URL(URL).readText()).getJSONArray("data")
            // go to last index
            val currentSeason = seasonsJSON.getJSONObject(seasonsJSON.length() - 1)
            val seasonName = currentSeason.getString("displayName")
            val seasonEnd = currentSeason.getString("endTime")
            val parentUUID = currentSeason.getString("parentUuid")

            // find the parent season
            for (i in 0 until seasonsJSON.length()) {
                val season = seasonsJSON.getJSONObject(i)
                if (season.getString("uuid") == parentUUID) {
                    val parentName = season.getString("displayName")
                    uiThread {
                        val seasonName = "$seasonName - $parentName"
                        binding.newCurrentSeason.text = seasonName

                        // parse the seasonEnd date
                        val date = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'").parse(seasonEnd)
                        val formatter = SimpleDateFormat("dd MMM yyyy")
                        val formattedDate = date?.let { it1 -> formatter.format(it1) }
                        // convert formatter date to unix time
                        val unixTime = date?.time?.div(1000)
                        val timeLeft = timeAgo(unixTime!!)
                        binding.newCurrentSeasonEnding.text = "$formattedDate ($timeLeft)"
                    }
                    break
                }
            }
        }
    }

    private fun updateTimer() {
        // only one timer is needed
        if (timer != null) {
            timer!!.cancel()
        }
        timer = object : CountDownTimer(60000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                binding.newMainMenuToolBar.subtitle =
                    "Next update in ${millisUntilFinished / 1000} seconds"
            }

            override fun onFinish() {
                binding.newMainMenuToolBar.subtitle = "Updating..."
                getLatestDetails()
            }
        }
        (timer as CountDownTimer).start()
    }

    private fun stopTimer() {
        REFRESHING = true
        timer?.cancel()
        binding.newMainMenuToolBar.subtitle = "Refreshing..."
    }

    private fun loadMatchDetails(lastMatchData: JSONObject) {
        val db = AssetsDatabase(this)

        val metaData = lastMatchData.getJSONObject("metadata")
        binding.newMatchMapName.text = metaData.getString("map")

        val mapImage = db.retrieveImage(metaData.getString("map"))
        val blurred = myblur(mapImage, this)
        binding.newMatchMapImage.setImageBitmap(blurred)

        binding.newMatchGameMode.text = metaData.getString("mode")
        binding.newMatchRegion.text = metaData.getString("cluster")

        val timePlayed = timeAgo(metaData.getLong("game_start"))
        binding.newMatchMapDate.text = timePlayed

        val playerData = lastMatchData.getJSONObject("players")
        binding.newMatchKDA.text = getKDA(playerData.getJSONArray("all_players"))
    }

    private fun getKDA(playerJSON: JSONArray): String {
        // loop through the player array until playername is found
        for (i in 0 until playerJSON.length()) {
            val player = playerJSON.getJSONObject(i)
            if (player.getString("name") + "#" + player.getString("tag") == playerName) {
                val kills = player.getJSONObject("stats").getInt("kills")
                val deaths = player.getJSONObject("stats").getInt("deaths")
                val assists = player.getJSONObject("stats").getInt("assists")

                val agentImage =
                    player.getJSONObject("assets").getJSONObject("agent").getString("small")
                Picasso.get().load(agentImage).into(binding.newMatchAgentImage)
                processRoundNumbers(player.getString("team").lowercase(Locale.ROOT))
                return "$kills/$deaths/$assists"
            }
        }
        return "0/0/0"
    }

    private fun processRoundNumbers(team: String) {
        val teams = lastMatchData?.getJSONObject("teams")
        if (team == "red") {
            val teamScore = teams?.getJSONObject("red")?.getInt("rounds_won")
            binding.newMatchAllyScore.text = teamScore.toString()
            binding.newMatchEnemyScore.text =
                teams?.getJSONObject("blue")?.getInt("rounds_won").toString()

            // make ally score valorant red and enemy score valorant blue
            binding.newMatchAllyScore.setTextColor(Color.parseColor("#f94555"))
            binding.newMatchEnemyScore.setTextColor(Color.parseColor("#18e4b7"))
        } else if (team == "blue") {
            val teamScore = teams?.getJSONObject("blue")?.getInt("rounds_won")
            binding.newMatchAllyScore.text = teamScore.toString()
            binding.newMatchEnemyScore.text =
                teams?.getJSONObject("red")?.getInt("rounds_won").toString()

            // make ally score valorant blue and enemy score valorant red
            binding.newMatchAllyScore.setTextColor(Color.parseColor("#18e4b7"))
            binding.newMatchEnemyScore.setTextColor(Color.parseColor("#f94555"))
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
}
