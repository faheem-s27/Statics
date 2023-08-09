package com.jawaadianinc.valorant_stats.valo.activities.new_ui.RecentMatchStats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.AgentStats
import com.jawaadianinc.valorant_stats.valo.classes.Agent
import com.squareup.picasso.Picasso
import java.math.BigDecimal

class RecentMatchStatsAgentAdapter(private val agents: Array<Pair<String, AgentStats>>) :
    RecyclerView.Adapter<RecentMatchStatsAgentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recentmatchstats_agents_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val agent = agents[position]
        Picasso.get().load("https://media.valorant-api.com/agents/${agent.first}/fullportrait.png").fit().centerCrop().into(holder.agentImage)
        holder.agentAmount.text = "Played ${agent.second.timesPlayed} times"
        holder.kills.text = "${agent.second.kills} kills"
        holder.deaths.text = "${agent.second.deaths} deaths"
        holder.assists.text = "${agent.second.assists} assists"
        val KD = roundToTwoDecimalPlaces((agent.second.kills.toDouble() / agent.second.deaths.toDouble()))
        holder.KD.text = "${KD} K/D"

        Picasso
            .get()
            .load("https://media.valorant-api.com/agents/${agent.first}/abilities/ability1/displayicon.png")
            .fit().centerInside()
            .into(holder.ability1)
        Picasso
            .get()
            .load("https://media.valorant-api.com/agents/${agent.first}/abilities/ability2/displayicon.png")
            .fit().centerInside()
            .into(holder.ability2)
        Picasso
            .get()
            .load("https://media.valorant-api.com/agents/${agent.first}/abilities/grenade/displayicon.png")
            .fit().centerInside()
            .into(holder.grenade)
        Picasso
            .get()
            .load("https://media.valorant-api.com/agents/${agent.first}/abilities/ultimate/displayicon.png")
            .fit().centerInside()
            .into(holder.ult)

        holder.ability1Text.text = agent.second.ability?.ability1Casts.toString()
        holder.ability2Text.text = agent.second.ability?.ability2Casts.toString()
        holder.grenadeText.text = agent.second.ability?.grenadeCasts.toString()
        holder.ultText.text = agent.second.ability?.ultimateCasts.toString()
    }

    override fun getItemCount(): Int {
        return agents.size
    }

    private fun roundToTwoDecimalPlaces(number: Double): Double {
        return String.format("%.2f", number).toDouble()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val agentImage: ImageView = itemView.findViewById(R.id.imageView)
        val agentAmount: TextView = itemView.findViewById(R.id.textView)
        val kills: TextView = itemView.findViewById(R.id.agentkills)
        val deaths: TextView = itemView.findViewById(R.id.agentdeaths)
        val assists: TextView = itemView.findViewById(R.id.agentassists)
        val KD: TextView = itemView.findViewById(R.id.agent_kda)
        val ability1: ImageView = itemView.findViewById(R.id.ability1)
        val ability2: ImageView = itemView.findViewById(R.id.ability2)
        val grenade: ImageView = itemView.findViewById(R.id.grenade)
        val ult: ImageView = itemView.findViewById(R.id.ultimate)
        val ultText: TextView = itemView.findViewById(R.id.ultNumber)
        val ability1Text: TextView = itemView.findViewById(R.id.ability1Number)
        val ability2Text: TextView = itemView.findViewById(R.id.ability2Text)
        val grenadeText: TextView = itemView.findViewById(R.id.grenadeNumber)

    }

}
