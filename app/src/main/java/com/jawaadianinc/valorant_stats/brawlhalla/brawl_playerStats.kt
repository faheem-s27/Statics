package com.jawaadianinc.valorant_stats.brawlhalla

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import org.json.JSONArray
import org.json.JSONObject
import java.util.*
import kotlin.math.roundToInt

class brawl_playerStats : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brawl_player_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val json = JSONObject(brawlStatsActivity.playerStatsJSON.toString())
        val brawlName: TextView = view.findViewById(R.id.brawlName)
        val brawlProgress: ProgressBar = view.findViewById(R.id.brawlProgress)
        val brawlLevel: TextView = view.findViewById(R.id.brawlLevel)
        val brawlXP: TextView = view.findViewById(R.id.brawlXP)
        val brawlGames: TextView = view.findViewById(R.id.brawlGames)
        val brawlWins: TextView = view.findViewById(R.id.brawlWins)
        val brawlLosses: TextView = view.findViewById(R.id.brawlLosses)
        val brawlLegendName: TextView = view.findViewById(R.id.brawlLegendName)

        val xp = json.getString("xp")
        val xpPercent = json.getString("xp_percentage").toDouble() * 100
        val level = json.getString("level")
        val totalGames = json.getString("games").toInt()
        val wins = json.getString("wins").toInt()
        val losses = totalGames - wins
        val legendsArray: JSONArray = json.getJSONArray("legends")

        lateinit var legendName: String
        lateinit var legendKOs: String
        lateinit var legendFalls: String
        lateinit var legendSuicides: String
        lateinit var legendGames: String
        lateinit var legendWins: String
        lateinit var legendLosses: String
        lateinit var legendXP: String
        lateinit var legendLevel: String
        lateinit var legendXPpercantage: String
        lateinit var legendID: String
        var timePlayed = 0

        for (i in 0 until legendsArray.length()) {
            val currentLegend = legendsArray[i] as JSONObject
            val currentTime = currentLegend.getString("matchtime")
            if (currentTime.toInt() > timePlayed) {
                timePlayed = currentTime.toInt()
                legendName = currentLegend.getString("legend_name_key")

            }
        }

        try {
            brawlLegendName.text = "Most played legend " + legendName.replaceFirstChar {
                if (it.isLowerCase()) it.titlecase(
                    Locale.getDefault()
                ) else it.toString()
            }
        } catch (e: kotlin.UninitializedPropertyAccessException) {
            Toast.makeText(
                requireActivity(),
                json.getString("name") + " has not played enough to get stats!",
                Toast.LENGTH_SHORT
            ).show()
            requireActivity().finish()
        }

        brawlGames.text = "Total $totalGames"
        brawlWins.text = "Wins: $wins"
        brawlLosses.text = "Losses: $losses"
        brawlLevel.text = "Level $level"
        brawlProgress.max = 100
        brawlProgress.progress = xpPercent.roundToInt()
        brawlXP.text = "XP: " + xp
        brawlName.text = json.getString("name") + "'s stats"


    }

}
