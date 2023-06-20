package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.RequestLog
import com.jawaadianinc.valorant_stats.valo.classes.Player
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.squareup.picasso.Picasso

class RequestLogAdapter(
    private val context: Activity,
    private val Logs: ArrayList<RequestLog>
) : ArrayAdapter<Any?>(
    context, R.layout.request_log_layout, Logs as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.request_log_layout, null, true)
        val urlTextView = row!!.findViewById<View>(R.id.log_URL) as TextView
        val methodTextView = row.findViewById<View>(R.id.log_method) as TextView
        val timeTextView = row.findViewById<View>(R.id.log_time) as TextView
        val codeTextView = row.findViewById<View>(R.id.log_code) as TextView

        val Log = Logs[position]

        urlTextView.text = Log.url
        methodTextView.text = Log.method
        timeTextView.text = Log.dateTime
        codeTextView.text = Log.code.toString()

        if (Log.code.toString().startsWith("2"))
        {
            codeTextView.setTextColor(android.graphics.Color.parseColor("#00FF00"))
        }
        // check if the code starts with 4 or 5
        else if (Log.code.toString().startsWith("4") || Log.code.toString().startsWith("5"))
        {
            codeTextView.setTextColor(android.graphics.Color.parseColor("#FF0000"))
        }

        // only animate the newest player
        if (position == Logs.size - 1) {
            // animate them coming in from the side
            row.translationX = -1000f
            row.animate().translationXBy(1000f).setDuration(500).setInterpolator {
                val t = it - 1.0f
                t * t * t * t * t + 1.0f
            }.start()
        }

        return row
    }
}
