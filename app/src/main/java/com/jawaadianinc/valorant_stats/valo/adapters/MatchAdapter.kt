package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import java.time.Duration
import java.time.Instant
import java.util.*

class MatchAdapter(
    private val context: Activity,
    private val agentURL: ArrayList<String>,
    private val mapURL: ArrayList<String>,
    private val timePlayed: ArrayList<String>,
    private val KDA: ArrayList<String>,
    private val mode: ArrayList<String>,
    private val won: ArrayList<String>,
) : ArrayAdapter<Any?>(
    context, R.layout.match_row, agentURL as List<Any?>
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

        when {
            won[position] == "true" -> {
                wonBar.setBackgroundColor(Color.parseColor("#18e4b7"))
            }
            won[position] == "false" -> {
                wonBar.setBackgroundColor(Color.parseColor("#ff0000"))
            }
            else -> {
                wonBar.setBackgroundColor(Color.parseColor("#FF111822"))
            }
        }

        Picasso
            .get()
            .load(agentURL[position])
            .fit()
            .centerInside()
            .into(agentImage)
        Picasso.get().load(mapURL[position]).transform(BlurTransformation(context, 2, 2)).fit()
            .centerInside().into(mapImage)

        mapImage.clipToOutline = true

        when {
            mode[position] == "" -> {
                modeText.text = "Custom Game"
            }
            else -> {
                modeText.text = mode[position].replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase(
                        Locale.getDefault()
                    ) else it.toString()
                }
            }
        }

        kdaText.text = KDA[position]

        val date = Date(timePlayed[position].toLong())
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

        return row
    }

}
