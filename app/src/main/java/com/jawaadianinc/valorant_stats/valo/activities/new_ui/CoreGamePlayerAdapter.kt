package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import okhttp3.Request
import org.json.JSONObject

class CoreGamePlayerAdapter(private val players: List<CoreGamePlayer>) :
    RecyclerView.Adapter<CoreGamePlayerAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.live_match_pregame_agent_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val player = players[position]
        holder.playerName.text = player.name
        holder.agentCharacter.text = player.agentName

        holder.agentImage.alpha = 1f

        if (player.name.length > 8) {
            holder.playerName.textSize = 10f
        }

        when {
            player.name == "You" -> {
                holder.agentImage.setBackgroundResource(R.drawable.yellow_to_black)
                holder.playerName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.Valorant_Yellow))
            }
            player.team == "Red" -> {
                holder.agentImage.setBackgroundResource(R.drawable.red_to_black)
                holder.playerName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.Valorant_Red))
            }
            else -> {
                holder.agentImage.setBackgroundResource(R.drawable.blue_to_black)
                holder.playerName.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.Valorant_Blue))
            }
        }

        holder.agentCharacter.setTextColor(holder.itemView.context.resources.getColor(R.color.white))

        Picasso
            .get()
            .load(player.getAgentImage())
            .fit()
            .centerInside()
            .into(holder.agentImage)

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
