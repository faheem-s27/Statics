package com.jawaadianinc.valorant_stats.valo.activities.ui.agents

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.TGGAgentsAdapter
import com.jawaadianinc.valorant_stats.valo.classes.Agents
import com.jawaadianinc.valorant_stats.valo.databases.TrackerDB
import org.json.JSONObject

class AgentsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragments_trackergg_agents, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = TrackerDB(requireActivity())
        val playerName = requireActivity().intent.getStringExtra("playerName")
        val mode = requireActivity().intent.getStringExtra("mode")

        val agentsJSON = JSONObject(db.getAgentsJSON(playerName!!, mode!!)!!)
        val listView = view.findViewById<ListView>(R.id.trackerAgentListView)

        val agents = agentsJSON.getJSONArray("data")
        val agentsList = mutableListOf<Agents>()
        for (i in 0 until agents.length()) {
            val currentAgent = agents.getJSONObject(i)
            if (currentAgent.getString("type") == "agent") {
                val name = currentAgent.getJSONObject("metadata").getString("name")
                val image = currentAgent.getJSONObject("metadata").getString("imageUrl")
                val stats = currentAgent.getJSONObject("stats")
                val totalMatches = stats.getJSONObject("matchesPlayed").getString("displayValue")
                val kills = stats.getJSONObject("killsPerMatch").getString("displayValue")
                val deaths = stats.getJSONObject("deathsPerMatch").getString("displayValue")
                val assists = stats.getJSONObject("assistsPerMatch").getString("displayValue")
                val kd = stats.getJSONObject("kDRatio").getString("displayValue")
                val winrate = stats.getJSONObject("matchesWinPct").getString("displayValue")

                agentsList.add(
                    Agents(
                        name,
                        image,
                        totalMatches.toInt(),
                        kills.toDouble(),
                        deaths.toDouble(),
                        assists.toDouble(),
                        kd.toDouble(),
                        winrate
                    )
                )

            }
        }
        val adapter = TGGAgentsAdapter(requireActivity(), agentsList)
        // sort the list by most matches played
        agentsList.sortByDescending { it.matches }
        listView.adapter = adapter
    }
}
