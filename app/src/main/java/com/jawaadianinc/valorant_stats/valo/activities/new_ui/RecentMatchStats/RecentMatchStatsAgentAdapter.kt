package com.jawaadianinc.valorant_stats.valo.activities.new_ui.RecentMatchStats

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class RecentMatchStatsAgentAdapter(private val agents: Array<Pair<String, Int>>) :
    RecyclerView.Adapter<RecentMatchStatsAgentAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recentmatchstats_agents_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val agent = agents[position]
        Picasso.get().load("https://media.valorant-api.com/agents/${agent.first}/fullportrait.png").fit().centerCrop().into(holder.agentImage)
        holder.agentAmount.text = "Played ${agent.second} times"

//        when (position)
//        {
//            0 -> holder.agentImage.setBackgroundResource(R.drawable.first_border)
//            1 -> holder.agentImage.setBackgroundResource(R.drawable.second_border)
//            2 -> holder.agentImage.setBackgroundResource(R.drawable.third_border)
//        }
    }

    override fun getItemCount(): Int {
        return agents.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val agentImage: ImageView = itemView.findViewById(R.id.imageView)
        val agentAmount: TextView = itemView.findViewById(R.id.textView)
    }

}
