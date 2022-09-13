package com.jawaadianinc.valorant_stats.valo.activities.ui.maps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
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
        val playerName = PlayerDatabase(requireActivity()).getPlayerName()

        val mapsJSON = JSONObject(db.getMapsJSON(playerName!!)!!)

    }
}
