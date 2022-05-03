package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class AgentAbilityAdapter(
    private val context: Activity,
    private val abilityName: ArrayList<String>,
    private val abilityDesc: ArrayList<String>,
    private val abilityImage: ArrayList<String>
) : ArrayAdapter<Any>(
    context, R.layout.agent_abilities_layout, abilityDesc as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.agent_abilities_layout, null, true)

        val abilityNameText = row!!.findViewById<TextView>(R.id.agentAbilityTitle) as TextView
        val abilityDescText = row.findViewById<TextView>(R.id.agentAbilityDescription) as TextView
        val abilityImageView = row.findViewById<ImageView>(R.id.agentAbilityIcon) as ImageView

        abilityNameText.text = abilityName[position]
        abilityDescText.text = abilityDesc[position]
        Picasso.get().load(abilityImage[position]).fit().centerInside().into(abilityImageView)

        return row

    }
}
