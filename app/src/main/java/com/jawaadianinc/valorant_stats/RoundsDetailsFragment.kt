package com.jawaadianinc.valorant_stats

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class RoundsDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rounds_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val Name = requireActivity().intent.extras!!.getString("RiotName")
        val ID = requireActivity().intent.extras!!.getString("RiotID")
        val MatchNumber = requireActivity().intent.extras!!.getInt("MatchNumber")

        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID"

        doAsync {
            try {
                val matchhistoryURL = URL(allmatches).readText()
                val jsonMatches = JSONObject(matchhistoryURL)
                val data = jsonMatches["data"] as JSONArray
                val easier = data.getJSONObject(MatchNumber).getJSONObject("metadata")
                val matchID = easier.getString("matchid")

                val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"

                val matchdetailsURL = URL(matchURl).readText()
                val jsonDetails = JSONObject(matchdetailsURL)
                val matchData = jsonDetails["data"] as JSONObject

                val rounds = matchData.getJSONArray("rounds")

                uiThread {

                val arrayList = ArrayList<String>()
                val roundsListView : ListView = view.findViewById(R.id.RoundsList)
                val mAdapter = object :
                    ArrayAdapter<String?>(
                        activity?.applicationContext!!, android.R.layout.simple_list_item_1,
                        arrayList as List<String?>
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

                roundsListView.adapter = mAdapter

                for (i in 0 until rounds.length()) {
                    val roundDetails = rounds[i] as JSONObject
                    val winning_team = roundDetails.getString("winning_team")
                    val ending = roundDetails.getString("end_type")
                    val number = i + 1
                    mAdapter.add("Round $number, Team Won: $winning_team, won by $ending")



                    }
                }

            } catch (e:Exception){
                uiThread {
                    AlertDialog.Builder(requireActivity()).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }

    }

}