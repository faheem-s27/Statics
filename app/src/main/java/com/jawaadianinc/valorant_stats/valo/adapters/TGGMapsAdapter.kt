package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.classes.Maps
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation

class TGGMapsAdapter(private val context: Activity, private val maps: List<Maps>) :
    ArrayAdapter<Any?>(
        context, R.layout.mmr_layout, maps as List<Any?>
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.tracker_maps_layout, null, true)

        val mapImage = row?.findViewById<View>(R.id.tgg_mapImage) as ImageView
        val totalMatches = row.findViewById<View>(R.id.tgg_totalMatchesText) as TextView
        val totalTime = row.findViewById<View>(R.id.tgg_totalTime) as TextView
        val headShotRatio = row.findViewById<View>(R.id.tgg_hs) as TextView
        val mostKills = row.findViewById<View>(R.id.tgg_mostKills) as TextView
        val aces = row.findViewById<View>(R.id.tgg_aces) as TextView
        val clutches = row.findViewById<View>(R.id.tgg_clutch) as TextView
        val winRate = row.findViewById<View>(R.id.tgg_winRate) as TextView
        val totalKills = row.findViewById<View>(R.id.tgg_totalKills) as TextView
        val totalDeaths = row.findViewById<View>(R.id.tgg_totalDeaths) as TextView
        val totalAssists = row.findViewById<View>(R.id.tgg_totalAssists) as TextView
        val attackWinRate = row.findViewById<View>(R.id.tgg_attackWinRate) as TextView
        val defenseWinRate = row.findViewById<View>(R.id.tgg_defenseWinRate) as TextView

        val map = maps[position]

        Picasso
            .get()
            .load(map.image)
            .transform(BlurTransformation(context, 2, 2))
            .into(mapImage)
        totalMatches.text = "Played " + map.totalMatches + " matches"
        totalTime.text = "Total time: " + map.totalTime
        headShotRatio.text = "Headshot Percantage: " + map.headShotPercentage
        mostKills.text = "Most kills in match: " + map.mostKills.toString()
        aces.text = "Aces: " + map.aces.toString()
        clutches.text = "Clutches: " + map.clutches.toString()
        winRate.text = "WinRate: " + map.winRate
        totalKills.text = "Kills: " + map.kills.toString()
        totalDeaths.text = "Deaths: " + map.deaths.toString()
        totalAssists.text = "Assists: " + map.assists.toString()
        attackWinRate.text = "Attack WinRate: " + map.attackWinRate
        defenseWinRate.text = "Defense WinRate: " + map.defenseWinRate

        return row


    }
}
