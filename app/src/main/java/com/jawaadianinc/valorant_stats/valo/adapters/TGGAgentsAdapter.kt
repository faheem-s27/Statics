package com.jawaadianinc.valorant_stats.valo.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.palette.graphics.Palette
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.classes.Agents
import com.squareup.picasso.Picasso

class TGGAgentsAdapter(private val context: Activity, private val agents: List<Agents>) :
    ArrayAdapter<Any?>(
        context, R.layout.mmr_layout, agents as List<Any?>
    ) {
    @SuppressLint("SetTextI18n")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.tracker_agent_layout, null, true)

        val agentImage = row?.findViewById<ImageView>(R.id.trackerAgent_image)
        val agentName = row?.findViewById<TextView>(R.id.trackerAgent_Name)
        val agentKDA = row?.findViewById<TextView>(R.id.trackerAgent_KDA)
        val agentKDAdetailed = row?.findViewById<TextView>(R.id.trackerAgent_KDAdetailed)
        val agentWinRate = row?.findViewById<TextView>(R.id.trackerAgent_WinRate)
        val agentMatches = row?.findViewById<TextView>(R.id.trackerAgent_Matches)
        val agent = agents[position]
        Picasso
            .get()
            .load(agent.image)
            .into(object : com.squareup.picasso.Target {
                override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                }

                override fun onBitmapFailed(
                    e: java.lang.Exception?,
                    errorDrawable: android.graphics.drawable.Drawable?
                ) {
                }

                override fun onBitmapLoaded(
                    bitmap: android.graphics.Bitmap?,
                    from: Picasso.LoadedFrom?
                ) {
                    agentImage?.setImageBitmap(bitmap)
                    // get the most dominant color from the bitmap
                    val color = Palette.generate(bitmap)
                        .getDominantColor(context.resources.getColor(R.color.Valorant_SplashColourBackground))
                    val color2 = Palette.generate(bitmap)
                        .getVibrantColor(context.resources.getColor(R.color.Valorant_SplashColourBackground))
                    val gradientDrawable = android.graphics.drawable.GradientDrawable(
                        android.graphics.drawable.GradientDrawable.Orientation.LEFT_RIGHT,
                        intArrayOf(color, R.color.black)
                    )
                    gradientDrawable.cornerRadius = 0f
                    row?.background = gradientDrawable
                }
            })
        agentMatches!!.text = "${agent.matches} matches"
        agentWinRate!!.text = agent.winrate
        val kdaDetailed = "${agent.kills}/${agent.deaths}/${agent.assists}"
        agentKDAdetailed!!.text = kdaDetailed
        agentName!!.text = agent.name
        agentKDA!!.text = agent.kd.toString()

        return row!!


    }
}
