package com.jawaadianinc.valorant_stats.valo.activities.ui.weapons

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.databases.TrackerDB
import org.json.JSONObject

class WeaponsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragments_trackergg_weapons, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val db = TrackerDB(requireActivity())
        val playerName = requireActivity().intent.getStringExtra("playerName")
        val mode = requireActivity().intent.getStringExtra("mode")

        val weapons = JSONObject(db.getWeaponsJSON(playerName!!, mode!!)!!)

    }
}
