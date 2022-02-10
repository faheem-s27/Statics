package com.jawaadianinc.valorant_stats.brawlhalla

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class brawl_playerClan : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_brawl_player_clan, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val json = brawlStatsActivity.playerClanJSON
        val tv: TextView = view.findViewById(R.id.textView)
        if (json == null) {
            tv.text = "Player is not in a clan"
        } else {
            handleClanJSON(JSONObject(json.toString()))
        }
    }

    fun handleClanJSON(json: JSONObject) {
        val tvClanName: TextView = requireView().findViewById(R.id.textView)
        val long = json.getLong("clan_create_date")
        val simpleDateFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm:ss", Locale.ENGLISH)
        val createdDate = simpleDateFormat.format(long * 1000L)
        tvClanName.text = "Name: " + json.getString("clan_name") + "\nCreated: " + createdDate
    }

}
