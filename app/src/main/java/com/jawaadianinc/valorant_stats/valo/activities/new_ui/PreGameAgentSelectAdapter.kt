package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.graphics.Typeface
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
        if (player.name == "You")
        {
            holder.agentImage.setBackgroundResource(R.drawable.yellow_to_black)
            // set text colour to #f9eaac
            holder.playerName.setTextColor(holder.itemView.context.resources.getColor(R.color.Valorant_Yellow))
        }

        when (player.agentSelectState) {
            "" -> {
                holder.agentCharacter.text = "Picking..."
            }
            "selected" -> {
                holder.agentCharacter.text = "Selecting agent..."
            }
            "locked" -> {
                //holder.agentCharacter.text = "Locked in!"
                holder.agentImage.alpha = 1f
                holder.agentCharacter.text = player.agentName
                // make it white
                holder.agentCharacter.setTextColor(holder.itemView.context.resources.getColor(R.color.white))
                // make it bold
                holder.agentCharacter.setTypeface(null, Typeface.BOLD)
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
