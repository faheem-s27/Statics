package com.jawaadianinc.valorant_stats.valo.activities.ui.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.TGGMapsAdapter
import com.jawaadianinc.valorant_stats.valo.classes.Maps
import com.jawaadianinc.valorant_stats.valo.databases.TrackerDB
import org.json.JSONObject

class MapsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragments_trackergg_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val db = TrackerDB(requireActivity())
        val playerName = requireActivity().intent.getStringExtra("playerName")
        val mode = requireActivity().intent.getStringExtra("mode")

        val listView = view.findViewById<ListView>(R.id.mapsListView)
        val listofMaps = ArrayList<Maps>()

        val mapsAdapter = TGGMapsAdapter(requireActivity(), listofMaps)
        listView.adapter = mapsAdapter

        val mapsJSON = JSONObject(db.getMapsJSON(playerName!!, mode!!)!!)

        Log.d("TrackerGG", mapsJSON.toString())

        val data = mapsJSON.getJSONArray("data")

        for (i in 0 until data.length()) {
            try {
                val currentMap = data.getJSONObject(i)
                if (currentMap.getString("type") == "map") {
                    Log.d("TrackerGG", "I ran")
                    val image = currentMap.getJSONObject("metadata").getString("imageUrl")
                    val totalMatches =
                        currentMap.getJSONObject("stats").getJSONObject("matchesPlayed")
                            .getString("displayValue")
                    val totalTime = currentMap.getJSONObject("stats").getJSONObject("timePlayed")
                        .getString("displayValue")
                    val headShotRatio =
                        currentMap.getJSONObject("stats").getJSONObject("headshotsPercentage")
                            .getString("displayValue")
                    val mostKills =
                        currentMap.getJSONObject("stats").getJSONObject("mostKillsInMatch")
                            .getString("displayValue")
                    val aces = currentMap.getJSONObject("stats").getJSONObject("aces")
                        .getString("displayValue")
                    val clutches = currentMap.getJSONObject("stats").getJSONObject("clutches")
                        .getString("displayValue")
                    val winRate = currentMap.getJSONObject("stats").getJSONObject("matchesWinPct")
                        .getString("displayValue")
                    val kills = currentMap.getJSONObject("stats").getJSONObject("kills")
                        .getString("displayValue")
                    val deaths = currentMap.getJSONObject("stats").getJSONObject("deaths")
                        .getString("displayValue")
                    val assists = currentMap.getJSONObject("stats").getJSONObject("assists")
                        .getString("displayValue")
                    val attackWinRate =
                        currentMap.getJSONObject("stats").getJSONObject("attackRoundsWinPct")
                            .getString("displayValue")
                    val defenseWinRate =
                        currentMap.getJSONObject("stats").getJSONObject("defenseRoundsWinPct")
                            .getString("displayValue")

                    val map = Maps(
                        image,
                        totalMatches,
                        totalTime,
                        headShotRatio,
                        mostKills,
                        aces,
                        clutches,
                        winRate,
                        kills,
                        deaths,
                        assists,
                        attackWinRate,
                        defenseWinRate
                    )

                    listofMaps.add(map)
                    mapsAdapter.notifyDataSetChanged()
                }

            } catch (e: Exception) {
                Log.d("TrackerGG", "Error: $e")
            }
        }
    }
}
