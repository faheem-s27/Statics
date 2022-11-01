package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.animation.DecelerateInterpolator
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityStaticsMainMenuBinding
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.activities.MMRActivity
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat

class StaticsMainMenu : AppCompatActivity() {
    lateinit var playerName: String
    var region: String? = null
    var key: String = ""
    lateinit var binding: ActivityStaticsMainMenuBinding
    var toolbar: MaterialToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the player name from the previous activity
        playerName = intent.getStringExtra("playerName").toString()
        region = intent.getStringExtra("region")
        key = intent.getStringExtra("key").toString()

        playerName = "noot#bozo"

        setup()
    }

    private fun setup() {
        toolbar = binding.newMainMenuToolBar
        toolbar!!.setTitleTextColor(resources.getColor(android.R.color.white))
        toolbar!!.title = playerName.split("#")[0]

        binding.newPlayerName.text = playerName.split("#")[0]
        binding.newPlayerTag.text = "#" + playerName.split("#")[1]

        Log.d("newMainMenu", "playerName: $playerName, region: $region")

        getLastMatch()
        getCurrentSeason()
    }

    private fun getLastMatch() {
        // split the player name into two parts by # and set it to RiotName and RiotID
        val playerNameSplit = playerName.split("#".toRegex()).toTypedArray()
        val riotName = playerNameSplit[0]
        val riotID = playerNameSplit[1]
        val allmatches =
            "https://api.henrikdev.xyz/valorant/v3/matches/$region/$riotName/$riotID?size=1"
        val ranksURL = "https://api.henrikdev.xyz/valorant/v1/mmr-history/$region/$riotName/$riotID"

        doAsync {
            val lastMatchData = Henrik(this@StaticsMainMenu).henrikAPI(allmatches)
            val ranksData = Henrik(this@StaticsMainMenu).henrikAPI(ranksURL)
            uiThread {
                loadRankDetails(ranksData)
                processPlayerDetails(lastMatchData)
            }
        }
    }

    private fun processPlayerDetails(matchData: JSONObject) {
        Toast.makeText(this, "Got last match!", Toast.LENGTH_LONG).show()
        val matchDataArray: JSONObject
        try {
            matchDataArray = matchData.getJSONArray("data").getJSONObject(0)
            val playersArray = matchDataArray.getJSONObject("players").getJSONArray("all_players")
            // iterate until we find the player we are looking for
            for (i in 0 until playersArray.length()) {
                val currentPlayer = playersArray.getJSONObject(i)
                if (currentPlayer.getString("name") == playerName.split("#")[0] && currentPlayer.getString(
                        "tag"
                    ) == playerName.split("#")[1]
                ) {
                    loadPlayerDetails(currentPlayer)
                    break
                }
            }
        } catch (e: Exception) {
            Log.d("newMainMenu", "Error from playerDetails: ${e.message}")
            // if there is an error, show the error message
            Toast.makeText(this, matchData.getString("message").toString(), Toast.LENGTH_LONG)
                .show()

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

    // make a function that takes a unix timestamp and returns how long ago it was
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
}
