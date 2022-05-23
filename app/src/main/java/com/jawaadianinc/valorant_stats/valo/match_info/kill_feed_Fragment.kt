package com.jawaadianinc.valorant_stats.valo.match_info

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.KillFeedAdapter
import org.json.JSONArray
import org.json.JSONObject

class kill_feed_Fragment : Fragment() {

    private var mapofPlayerandAgent: MutableMap<String, String> = mutableMapOf("player" to "agent")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_kill_feed_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val jsonDetails = MatchHistoryActivity.matchJSON
        val roundDetails = jsonDetails!!.getJSONObject("data").getJSONArray("rounds")
        val matchData = jsonDetails.get("data") as JSONObject

        val arrayList = ArrayList<String>()
        val arrayAdapter = object :
            ArrayAdapter<String>(
                activity?.applicationContext!!,
                android.R.layout.simple_spinner_item,
                arrayList
            ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val item = super.getView(position, convertView, parent) as TextView
                item.setTextColor(Color.parseColor("#FFFFFF"))
                item.setTypeface(item.typeface, Typeface.BOLD)
                item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
                return item
            }
        }
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner: Spinner =
            view.findViewById(R.id.roundSelect)
        spinner.adapter = arrayAdapter

        for (i in 0 until roundDetails.length()) {
            val roundNumber = i + 1
            arrayAdapter.add("Round $roundNumber")
        }

        try {
            val allPlayers =
                matchData.getJSONObject("players").getJSONArray("all_players") as JSONArray
            for (i in 0 until allPlayers.length()) {
                val data = allPlayers[i] as JSONObject
                val playerName = data.getString("name")
                val playerTag = data.getString("tag")
                val agentURL =
                    data.getJSONObject("assets").getJSONObject("agent").getString("small")
                val fullName = "$playerName#$playerTag"
                mapofPlayerandAgent[fullName] = agentURL
            }
        } catch (e: Exception) {
            Log.d("MatchError", e.toString())
        }

        spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val getRoundName =
                        spinner.getItemAtPosition(position).toString()
                    val numberinRound = getRoundName.split(" ")
                    val actualRound: Int = numberinRound[1].toInt() - 1
                    try {
                        populateKillFeed(actualRound)
                    } catch (e: Exception) {
                        Log.d("MatchError", e.toString())
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    fun populateKillFeed(roundNumber: Int) {
        val listView: ListView = requireView().findViewById(R.id.killFeedList)

        val killer = ArrayList<String>()
        val killerTeam = ArrayList<String>()
        val victim = ArrayList<String>()
        val victimTeam = ArrayList<String>()
        val weapon = ArrayList<String>()
        val weaponIcon = ArrayList<String>()

        val kills: JSONArray =
            MatchHistoryActivity.matchJSON!!.getJSONObject("data").getJSONArray("kills")
        for (i in 0 until kills.length()) {
            val round = kills.getJSONObject(i).get("round")
            if (round == roundNumber) {
                killer += mapofPlayerandAgent.getValue(
                    kills.getJSONObject(i).getString("killer_display_name")
                )
                killerTeam += kills.getJSONObject(i).getString("killer_team")
                victim += mapofPlayerandAgent.getValue(
                    kills.getJSONObject(i).getString("victim_display_name")
                )
                victimTeam += kills.getJSONObject(i).getString("victim_team")
                weapon += kills.getJSONObject(i).getString("damage_weapon_name")
                weaponIcon += kills.getJSONObject(i).getJSONObject("damage_weapon_assets")
                    .getString("killfeed_icon")
            }
        }

        val killList = KillFeedAdapter(
            requireActivity(),
            killer,
            killerTeam,
            victim,
            victimTeam,
            weapon,
            weaponIcon
        )
        listView.adapter = killList
    }

}
