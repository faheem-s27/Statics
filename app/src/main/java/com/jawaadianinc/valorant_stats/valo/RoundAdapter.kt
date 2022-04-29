package com.jawaadianinc.valorant_stats.valo

import android.app.Activity
import android.graphics.Color
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

        var colour = ""
        colour = if (teamWon[position] == "Blue") {
            "#18e4b7"
        } else {
            "#f94555"
        }

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
        roundNumberText.setBackgroundColor(Color.parseColor(colour))
        ending.setBackgroundColor(Color.parseColor(colour))
        roundIcon.setBackgroundColor(Color.parseColor(colour))
        return row
    }
}
