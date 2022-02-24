package com.jawaadianinc.valorant_stats.valorant

import android.app.Activity
import android.graphics.Color
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
    private val playerScore: ArrayList<String>,
    private val playerKills: ArrayList<String>,
    private val playerDeaths: ArrayList<String>,
    private val playerAssists: ArrayList<String>
) : ArrayAdapter<Any?>(
    context, R.layout.mmr_layout, agentURL as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.player_row, null, true)
        val agentImage = row!!.findViewById<View>(R.id.agentImage) as ImageView
        val playerNameText = row.findViewById<View>(R.id.NameOfPlayer) as TextView
        //val playerScoreText = row.findViewById<View>(R.id.Score) as TextView
        val KDA = row.findViewById<View>(R.id.KDA) as TextView
        var color = ""
        color = if (playerTeam[position] == "Blue") {
            "#18e4b7"
        } else {
            "#f94555"
        }
        Picasso
            .get()
            .load(agentURL[position])
            .fit()
            .centerCrop()
            .into(agentImage)

        val playerOnlyName = playerName[position].split("#")
        playerNameText.text = playerOnlyName[0]
        //playerScoreText.text = playerScore[position]
        KDA.text =
            "${playerKills[position]} / ${playerDeaths[position]} / ${playerAssists[position]}"

        //Setting colour
        playerNameText.setBackgroundColor(Color.parseColor(color))
        KDA.setBackgroundColor(Color.parseColor(color))

        return row
    }
}
