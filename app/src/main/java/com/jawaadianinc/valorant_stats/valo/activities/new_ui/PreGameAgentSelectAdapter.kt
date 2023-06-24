package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class PreGameAgentSelectAdapter(private val players: List<PreGameAgentSelectPlayer>) :
    RecyclerView.Adapter<PreGameAgentSelectAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.live_match_pregame_agent_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.playerName.text = player.name

        when (player.agentSelectState) {
            "" -> {
                holder.agentCharacter.text = "Picking..."
            }
            "selected" -> {
                holder.agentCharacter.text = "Selecting agent..."
            }
            "locked" -> {
                holder.agentCharacter.text = "Locked in!"
                holder.agentImage.alpha = 1f
            }
        }

        if (player.agentID != "")
        {
            Picasso
                .get()
                .load(player.getAgentImage())
                .fit()
                .centerInside()
                .into(holder.agentImage)
        }

        Picasso
            .get()
            .load(player.rankImage)
            .fit()
            .centerInside()
            .into(holder.playerRankImage)
    }

    override fun getItemCount(): Int {
        return players.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val agentImage: ImageView = itemView.findViewById(R.id.agent_select_agent)
        val playerName: TextView = itemView.findViewById(R.id.agent_select_username)
        val agentCharacter: TextView = itemView.findViewById(R.id.agent_select_user_agent)
        val playerRankImage: ImageView = itemView.findViewById(R.id.agent_select_rank)
    }
}
