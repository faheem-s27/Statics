package com.jawaadianinc.valorant_stats.valorant

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import org.json.JSONArray
import org.json.JSONObject

class kill_feed_Fragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_kill_feed_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val jsonDetails = MatchHistoryActivity.matchJSON
        val roundDetails = jsonDetails!!.getJSONObject("data").getJSONArray("rounds")

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
            view.findViewById<Spinner>(R.id.roundSelect)
        spinner.adapter = arrayAdapter

        for (i in 0 until roundDetails.length()) {
            val roundNumber = i + 1
            arrayAdapter.add("Round $roundNumber")
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
                    populateKillFeed(actualRound)
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                }
            }
    }

    fun populateKillFeed(roundNumber: Int) {
        val player_stats: JSONArray =
            MatchHistoryActivity.matchJSON!!.getJSONObject("data").getJSONArray("rounds")
                .getJSONObject(roundNumber).getJSONArray("player_stats")
        for (i in 0 until player_stats.length()) {
            val currentPlayer = player_stats[i] as JSONObject
            val killEvents: JSONArray = currentPlayer.getJSONArray("kill_events")
            for (k in 0 until killEvents.length()) {
                val currentKill = killEvents[k] as JSONObject
                val killerName = currentKill.getString("killer_display_name")
                val killerTeam = currentKill.getString("killer_team")
                val victimName = currentKill.getString("victim_display_name")
                val gun = currentKill.getString("damage_weapon_name")
                Log.d("kills", "$killerName killed $victimName with $gun")
            }
        }
    }

}
