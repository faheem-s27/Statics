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

class KillFeedAdapter(
    private val context: Activity,
    private val killerURL: ArrayList<String>,
    private val killerTeam: ArrayList<String>,
    private val victimURL: ArrayList<String>,
    private val victimTeam: ArrayList<String>,
    private val weapon: ArrayList<String>,
    private val weaponIcon: ArrayList<String>,
) : ArrayAdapter<Any?>(
    context, R.layout.match_row, killerURL as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.kill_row, null, true)

        val killerImage = row!!.findViewById<View>(R.id.killerIcon) as ImageView?
        val victimImage = row.findViewById<View>(R.id.victimIcon) as ImageView?
        val weaponImage = row.findViewById<View>(R.id.weaponIcon) as ImageView?
        val killerColour = row.findViewById(R.id.killColour) as View
        val victimColour = row.findViewById(R.id.victimColour) as View
        val gunName = row.findViewById<View>(R.id.gunName) as TextView

        if (killerTeam[position] == "Red") {
            killerColour.setBackgroundColor(Color.parseColor("#f94555"))
        } else {
            killerColour.setBackgroundColor(Color.parseColor("#18e4b7"))
        }

        if (victimTeam[position] == "Red") {
            victimColour.setBackgroundColor(Color.parseColor("#f94555"))
        } else {
            victimColour.setBackgroundColor(Color.parseColor("#18e4b7"))
        }

        weaponImage?.rotationY = 180f

        Picasso.get().load(killerURL[position]).into(killerImage)
        Picasso.get().load(victimURL[position]).into(victimImage)
        Picasso.get().load(weaponIcon[position]).into(weaponImage)
        gunName.text = "Killed using " + weapon[position]
        return row
    }
}
