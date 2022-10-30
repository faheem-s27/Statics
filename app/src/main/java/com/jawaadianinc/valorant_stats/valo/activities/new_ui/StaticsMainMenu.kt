package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.MaterialToolbar
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityStaticsMainMenuBinding
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject

class StaticsMainMenu : AppCompatActivity() {
    lateinit var playerName: String
    var region: String? = null
    lateinit var binding: ActivityStaticsMainMenuBinding
    var toolbar: MaterialToolbar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsMainMenuBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get the player name from the previous activity
        playerName = intent.getStringExtra("playerName").toString()
        region = intent.getStringExtra("region")

        toolbar = binding.newMainMenuToolBar
        toolbar!!.setTitleTextColor(resources.getColor(android.R.color.white))
        toolbar!!.title = playerName.split("#")[0]

        binding.newPlayerName.text = playerName.split("#")[0]
        binding.newPlayerTag.text = "#" + playerName.split("#")[1]

        Log.d("newMainMenu", "playerName: $playerName, region: $region")

        getLastMatch()

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
                processDetails(lastMatchData, ranksData)
            }
        }

    }

    private fun processDetails(matchData: JSONObject, ranksData: JSONObject) {
        Toast.makeText(this, "Got last match!", Toast.LENGTH_LONG).show()

        val matchDataArray = matchData.getJSONArray("data").getJSONObject(0)
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
        loadRankDetails(ranksData)
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
        val image = binding.newPlayerRankImage
        try {
            val rankData = rankHistory.getJSONArray("data").getJSONObject(0)
            val title = rankData.getString("currenttierpatched")
            val progress = rankData.getString("ranking_in_tier")
            val change = rankData.getString("mmr_change_to_last_game")
            val dateRaw = rankData.getString("date_raw")

            binding.newPlayerRankTimePlayed.text = "Last comped: ${timeAgo(dateRaw.toLong())}"

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
            binding.newRankProgressBar.progress = progress.toInt()
            binding.newPlayerRankTitle.text = title
            val rankImage =
                rankData.getJSONObject("images").getString("large")
            Picasso.get().load(rankImage).fit().centerInside().into(image)
        } catch (e: Exception) {
            Log.d("newMainMenu", "Error for rank: ${e.message}")
            Picasso.get()
                .load("https://media.valorant-api.com/competitivetiers/564d8e28-c226-3180-6285-e48a390db8b1/0/smallicon.png")
                .fit().centerInside().into(image)
            binding.newPlayerRankTitle.text = "Unranked"
            binding.newRankProgressBar.progress = 0
            binding.newPlayerRRText.text = "0"
        }
    }

    // make a function that takes a unix timestamp and returns how long ago it was
    private fun timeAgo(unixTime: Long): String {
        val time = unixTime * 1000
        val now = System.currentTimeMillis()
        if (time > now || time <= 0) {
            return "in the future"
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
}
