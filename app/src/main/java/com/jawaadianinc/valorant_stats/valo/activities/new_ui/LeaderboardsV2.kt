package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.json.JSONObject
import java.util.Locale

class LeaderboardsV2 : AppCompatActivity() {
    lateinit var region: String
    lateinit var playerName: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_leaderboards_v2)
        region = intent.getStringExtra("region").toString()
        playerName = intent.getStringExtra("playerName").toString()
        val playerCardID = intent.getStringExtra("playerImage").toString()

        val cardURL = "https://media.valorant-api.com/playercards/$playerCardID/largeart.png"
        val background = findViewById<ImageView>(R.id.leaderboardsBG)
        Picasso.get().load(cardURL).fit().centerCrop()
            .transform(BlurTransformation(this)).into(background)

        val tv = findViewById<TextView>(R.id.textView41)
        tv.text = "Leaderboards [${region.uppercase(Locale.getDefault())}]"

        getLeaderboards()

        val leaderboardRecyclerView = findViewById<ListView>(R.id.leaderboardV2_listview)
        // make it fast scroll
        leaderboardRecyclerView.isFastScrollEnabled = true
    }

    private fun getLeaderboards() {
        val leaderboardList = ArrayList<Leaderboard>()
        val leaderboardRecyclerView = findViewById<ListView>(R.id.leaderboardV2_listview)
        val url = "https://api.henrikdev.xyz/valorant/v2/leaderboard/$region"
        val queue = Volley.newRequestQueue(this)
        val stringRequest = StringRequest(
            Request.Method.GET, url,
            { response ->
                val json = JSONObject(response)
                val data = json.getJSONArray("players")
                val lastUpdate = json.getLong("last_update")
                val nextUpdate = json.getLong("next_update")
                val msg =
                    "${getString(R.string.s170)}: ${timeAgo(lastUpdate)}\n${getString(R.string.s171)}: ${
                        timeAgo(
                            nextUpdate
                        )
                    }"
                val tv = findViewById<TextView>(R.id.textView42)
                tv.text = msg

                for (i in 0 until data.length()) {
                    try {
                        val player = data.getJSONObject(i)
                        val name = player.getString("gameName") + "#" + player.getString("tagLine")
                        val rank = player.getInt("leaderboardRank")
                        val playerCardID = player.getString("PlayerCardID")
                        val mmr = player.getInt("rankedRating")
                        val wins = player.getInt("numberOfWins")
                        val leaderboard = Leaderboard(name, rank, playerCardID, mmr, wins)
                        leaderboardList.add(leaderboard)
                    } catch (_: Exception) {
                    }
                }
                val leaderboardAdapter = LeaderboardAdapter(this, leaderboardList)
                leaderboardRecyclerView!!.adapter = leaderboardAdapter
            },
            { error ->
                Toast.makeText(
                    this,
                    "Error getting leaderboards + $error",
                    Toast.LENGTH_SHORT
                ).show()
            })
        queue.add(stringRequest)

        val searchViewLeaderboards =
            findViewById<android.widget.SearchView>(R.id.searchViewLeaderboards)
        searchViewLeaderboards.setOnQueryTextListener(object :
            android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // make a new list of the filtered items
                // check if new text is empty
                if (newText!!.isEmpty()) {
                    // if it is empty, then show the original list
                    val leaderboardAdapter =
                        LeaderboardAdapter(this@LeaderboardsV2, leaderboardList)
                    leaderboardRecyclerView!!.adapter = leaderboardAdapter
                    return false
                }

                val filteredList = ArrayList<Leaderboard>()
                for (item in leaderboardList) {
                    if (item.name.lowercase(Locale.getDefault()).contains(
                            newText.toString()
                                .lowercase(Locale.getDefault())
                        )
                    ) {
                        filteredList.add(item)
                    }
                }
                // update the adapter with the new list
                val leaderboardAdapter = LeaderboardAdapter(this@LeaderboardsV2, filteredList)
                leaderboardRecyclerView!!.adapter = leaderboardAdapter
                return false
            }
        })
    }

    private fun timeAgo(unixTime: Long): String {
        // get the system time in milliseconds
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
            (diff / DAY_MILLIS).toString() + " days ago"
        }
    }
}
