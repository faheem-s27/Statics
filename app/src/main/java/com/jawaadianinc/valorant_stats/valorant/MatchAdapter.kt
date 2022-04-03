package com.jawaadianinc.valorant_stats.valorant

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
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
    private val matchIDs: ArrayList<String>,
) : ArrayAdapter<Any?>(
    context, R.layout.match_row, agentURL as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.match_row, null, true)

        val agentImage = row!!.findViewById<View>(R.id.agentImageRow) as ImageView
        val mapImage = row.findViewById<View>(R.id.mapImageRow) as ImageView
        val timePlayed_Text = row.findViewById<View>(R.id.TimeAgoRow) as TextView
        val KDA_Text = row.findViewById<View>(R.id.KDA_Row) as TextView
        val mode_Text = row.findViewById<View>(R.id.ModeRow) as TextView

        Picasso
            .get()
            .load(agentURL[position])
            .fit()
            .centerInside()
            .into(agentImage)
        Picasso.get().load(mapURL[position]).fit().centerInside().into(mapImage)

        if (mode[position] == "unrated") {
            mode_Text.text = "Unrated"
        } else if (mode[position] == "") {
            mode_Text.text = "Custom Game"
        } else {
            mode_Text.text = mode[position]
        }

        KDA_Text.text = KDA[position]
        val unixTimeStart = timePlayed[position].toLong()
        //Log.d("match", "timePlayed: $unixTimeStart")
        val date = Date(unixTimeStart)
        val d: Duration =
            Duration.between(
                date.toInstant(),
                Instant.now()
            )
        val timeinDays = d.toDays()
        val timeInHours = d.toHours()

        when {
            timeinDays > 0 -> {
                timePlayed_Text.text = "$timeinDays days ago"
            }
            timeInHours > 0 -> {
                timePlayed_Text.text = "$timeInHours hours ago"
            }
            else -> {
                timePlayed_Text.text = "${d.toMinutes()} minutes ago"
            }
        }
        return row
    }
}
