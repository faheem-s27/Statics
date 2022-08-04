package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class PlayerAdapter(
    private val context: Activity,
    private val agentURL: ArrayList<String>,
    private val playerTeam: ArrayList<String>,
    private val playerName: ArrayList<String>,
    private val playerKills: ArrayList<String>,
    private val playerDeaths: ArrayList<String>,
    private val playerAssists: ArrayList<String>,
    private val playerTiers: ArrayList<String>,
) : ArrayAdapter<Any?>(
    context, R.layout.mmr_layout, agentURL as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.player_row, null, true)
        val agentImage = row!!.findViewById<View>(R.id.agentImage) as ImageView
        val playerNameText = row.findViewById<View>(R.id.NameOfPlayer) as TextView
        val tierRankIcon = row.findViewById<View>(R.id.tierRankIcon) as ImageView

        val gridLayoutView = row.findViewById<View>(R.id.gridLayout2) as ViewGroup

        val KDA = row.findViewById<View>(R.id.KDA) as TextView
        Picasso
            .get()
            .load(agentURL[position])
            .fit()
            .centerCrop()
            .into(agentImage)

        val playerOnlyName = playerName[position].split("#")
        playerNameText.text = playerOnlyName[0]
        KDA.text =
            "${playerKills[position]} / ${playerDeaths[position]} / ${playerAssists[position]}"

        if (playerTiers[position] != "") {
            Picasso
                .get()
                .load(playerTiers[position])
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
        if (playerTeam[position] == "Blue") {
            gridLayoutView.background = context.getDrawable(R.drawable.blue_to_black)
        } else {
            gridLayoutView.background = context.getDrawable(R.drawable.red_to_black)
        }

        return row
    }
}
