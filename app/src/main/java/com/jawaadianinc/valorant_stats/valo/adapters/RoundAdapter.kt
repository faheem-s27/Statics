package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R

class RoundAdapter(
    private val context: Activity,
    private val teamWon: ArrayList<String>,
    private val endingType: ArrayList<String>,
    private val roundNumber: ArrayList<String>
) : ArrayAdapter<Any?>(
    context, R.layout.round_layout, teamWon as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.round_layout, null, true)
        val roundIcon = row!!.findViewById<View>(R.id.roundIcon) as ImageView
        val roundNumberText = row.findViewById<View>(R.id.RoundNumber) as TextView
        val ending = row.findViewById<View>(R.id.EndingType) as TextView
        val round_gridlayout = row.findViewById<View>(R.id.round_gridlayout) as ViewGroup

        roundNumberText.text = "Round ${roundNumber[position]}"
        ending.text = endingType[position]

        when {
            endingType[position] == "Eliminated" -> {
                roundIcon.setImageResource(R.drawable.eliminated)
            }
            endingType[position] == "Bomb defused" -> {
                roundIcon.setImageResource(R.drawable.spikedefused)
            }
            endingType[position] == "Round timer expired" -> {
                roundIcon.setImageResource(R.drawable.timerexpired)
            }
            endingType[position] == "Bomb detonated" -> {
                roundIcon.setImageResource(R.drawable.spikeexplode)
            }
        }

        if (teamWon[position] == "Blue") {
            round_gridlayout.background = context.getDrawable(R.drawable.blue_to_black)
        } else {
            round_gridlayout.background = context.getDrawable(R.drawable.red_to_black)
        }

        return row
    }
}
