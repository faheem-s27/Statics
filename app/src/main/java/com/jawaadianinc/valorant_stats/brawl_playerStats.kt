package com.jawaadianinc.valorant_stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import org.json.JSONObject

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
        val textView: TextView = view.findViewById(R.id.textView9)


    }

}
