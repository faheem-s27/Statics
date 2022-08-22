package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.classes.Player
import com.squareup.picasso.Picasso

class PlayersAdapter(
    private val context: Activity,
    private val Players: ArrayList<Player>
) : ArrayAdapter<Any?>(
    context, R.layout.mmr_layout, Players as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.player_row, null, true)
        val agentImage = row!!.findViewById<View>(R.id.agentImage) as ImageView
        val playerNameText = row.findViewById<View>(R.id.NameOfPlayer) as TextView
        val tierRankIcon = row.findViewById<View>(R.id.tierRankIcon) as ImageView
        val gridLayoutView = row.findViewById<View>(R.id.gridLayout2) as ViewGroup
        val kdaText = row.findViewById<View>(R.id.KDA) as TextView

        val player = Players[position]

        Picasso
            .get()
            .load(player.agent.url)
            .fit()
            .centerCrop()
            .into(agentImage)

        playerNameText.text = player.getNameAndTag().first
        kdaText.text =
            "${player.kills} / ${player.deaths} / ${player.assists}"

        if (player.rank.url != "") {
            Picasso
                .get()
                .load(player.rank.url)
                .fit()
                .centerCrop()
                .into(tierRankIcon)
        } else {
            Picasso
                .get()
                .load("https://media.valorant-api.com/competitivetiers/564d8e28-c226-3180-6285-e48a390db8b1/0/largeicon.png")
                .fit()
                .centerCrop()
                .into(tierRankIcon)
        }

        // if the player team is blue then the background color is blue else it is red
        if (player.team == "Blue") {
            gridLayoutView.background = context.getDrawable(R.drawable.blue_to_black)
        } else {
            gridLayoutView.background = context.getDrawable(R.drawable.red_to_black)
        }

        return row
    }
}
