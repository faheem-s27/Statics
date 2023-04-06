package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.widget.ArrayAdapter
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class LeaderboardAdapter(
    private val context: Activity,
    private val leaderboardList: ArrayList<Leaderboard>
) : ArrayAdapter<Any?>(context, R.layout.leaderboard_list, leaderboardList as List<Any?>) {
    override fun getView(
        position: Int,
        convertView: android.view.View?,
        parent: android.view.ViewGroup
    ): android.view.View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.leaderboard_list, null, true)
        val rankText = row!!.findViewById<android.widget.TextView>(R.id.leaderboardV2_rank)
        val nameText = row.findViewById<android.widget.TextView>(R.id.leaderboardV2_name)
        val mmrText = row.findViewById<android.widget.TextView>(R.id.leaderboardV2_rankRating)
        val playerCardImage = row.findViewById<android.widget.ImageView>(R.id.leaderboardV2_image)
        val wins = row.findViewById<android.widget.TextView>(R.id.leaderboardV2_wins)

        val leaderboard = leaderboardList[position]

        rankText.text = "#" + leaderboard.rank.toString()
        nameText.text = leaderboard.name
        mmrText.text = "Rating: " + leaderboard.mmr.toString()
        wins.text = "Wins: " + leaderboard.wins.toString()

        when (leaderboard.rank) {
            1 -> {
                rankText.setTextColor(android.graphics.Color.parseColor("#FFD700"))
            }
            2 -> {
                rankText.setTextColor(android.graphics.Color.parseColor("#C0C0C0"))
            }
            3 -> {
                rankText.setTextColor(android.graphics.Color.parseColor("#CD7F32"))
            }
            else -> {
                rankText.setTextColor(android.graphics.Color.parseColor("#FFFFFF"))
            }
        }

        if (leaderboard.rank.toString().length >= 3) {
            rankText.textSize = 20f
        } else if (leaderboard.rank.toString().length >= 5) {
            rankText.textSize = 10f
        }

        Picasso
            .get()
            .load(leaderboard.getSmallImage())
            .fit()
            .centerCrop()
            .into(playerCardImage)

        // animate them coming in from the side
        row.translationX = -1000f
        row.animate().translationXBy(1000f).setDuration(500).setInterpolator {
            val t = it - 1.0f
            t * t * t * t * t + 1.0f
        }.start()

        return row
    }

}
