package com.jawaadianinc.valorant_stats.valo.live_match

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R

class LiveMatchPlayerAdapter(
    private val context: Activity,
    private val Players: ArrayList<LiveMatchPlayer>
) : ArrayAdapter<Any?>(
    context, R.layout.mmr_layout, Players as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var rowView = convertView
        val inflater = context.layoutInflater
        if (convertView == null) rowView = inflater.inflate(R.layout.player_row, null, true)
        val player = Players[position]
        val title = rowView!!.findViewById<View>(R.id.voiceLinesName) as TextView
        val image = rowView.findViewById<View>(R.id.agentImage) as ImageView

        title.text = player.character
        image.setImageBitmap(player.image)

        // if the player team is blue then the background color is blue else it is red
        if (player.team == "Blue") {
            rowView.background = context.getDrawable(R.drawable.blue_to_black)
        } else {
            rowView.background = context.getDrawable(R.drawable.red_to_black)
        }

        // for each position in the row, animate the translation more than the previous one
//        for (i in 0..position) {
//            rowView.alpha = 0f
//            rowView.translationX = -1000f
//            rowView.animate()
//                .alpha(1f)
//                .translationXBy(1000f)
//                .setDuration(500)
//                .setInterpolator {
//                    val t = it - 1.0f
//                    t * t * t * t * t + 1.0f}
//                .setStartDelay(100 * i.toLong())
//                .start()
//        }

        return rowView
    }
}
