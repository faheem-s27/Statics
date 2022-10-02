package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.classes.Match
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import java.time.Duration
import java.time.Instant
import java.util.*

class MatchAdapter(
    private val context: Activity,
    private val Matches: ArrayList<Match>
) : ArrayAdapter<Any?>(
    context, R.layout.match_row, Matches as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.match_row, null, true)

        val agentImage = row!!.findViewById<View>(R.id.killerIcon) as ImageView
        val mapImage = row.findViewById<View>(R.id.mapImageRow) as ImageView
        val timeplayedText = row.findViewById<View>(R.id.TimeAgoRow) as TextView
        val kdaText = row.findViewById<View>(R.id.KDA_Row) as TextView
        val modeText = row.findViewById<View>(R.id.ModeRow) as TextView
        val wonBar = row.findViewById(R.id.wonbar) as View

        val match = Matches[position]

        when (match.won) {
            true -> {
                wonBar.setBackgroundColor(Color.parseColor("#18e4b7"))
            }
            false -> {
                wonBar.setBackgroundColor(Color.parseColor("#ff0000"))
            }
        }

        Picasso
            .get()
            .load(match.agentImage)
            .fit()
            .centerInside()
            .into(agentImage)
        Picasso.get().load(match.mapImage).transform(BlurTransformation(context, 2, 2)).fit()
            .centerInside().into(mapImage)

        mapImage.clipToOutline = true

        when (match.gameMode) {
            "" -> {
                modeText.text = "Custom Game"
            }
            else -> {
                modeText.text = match.gameMode.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
            }
        }

        kdaText.text = match.getKDA()

        val date = Date(match.timeStarted.toLong())
        val d: Duration =
            Duration.between(
                date.toInstant(),
                Instant.now()
            )

        // convert time to months
        val weeks = d.toDays() / 7
        val timeinDays = d.toDays()
        val timeInHours = d.toHours()

        when {
            weeks > 1 -> {
                timeplayedText.text = "$weeks weeks ago"
            }
            timeinDays > 1 -> {
                timeplayedText.text = "$timeinDays days ago"
            }
            timeInHours > 1 -> {
                timeplayedText.text = "$timeInHours hours ago"
            }
            else -> {
                timeplayedText.text = "${d.toMinutes()} minutes ago"
            }
        }

//        // only animate the last item
//        if (position == Matches.size - 1) {
//            row.alpha = 0f
//            row.translationY = 100f
//            row.animate()
//                .alpha(1f)
//                .translationY(0f)
//                .setDuration(500)
//                .setStartDelay(100)
//                .start()
//        }

        // delay each row by 100ms
        row.alpha = 0f
        row.translationY = 100f
        row.animate().alpha(1f).translationY(0f).setDuration(500).setInterpolator {
            val t = it - 1.0f
            t * t * t * t * t + 1.0f
        }.setStartDelay(100L)
            .start()
//
//        row.translationX = -1000f
//        row.animate().translationXBy(1000f).setDuration(500).setInterpolator {
//            val t = it - 1.0f
//            t * t * t * t * t + 1.0f
//        }.start()

        return row
    }

}
