package com.jawaadianinc.valorant_stats

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


class RoundsMoreDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rounds_more_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val Name = requireActivity().intent.extras!!.getString("RiotName")
        val ID = requireActivity().intent.extras!!.getString("RiotID")
        val MatchNumber = requireActivity().intent.extras!!.getInt("MatchNumber")

        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID?size=10"
        val spinnerRounds: Spinner = view.findViewById(R.id.roundsSpinner)

        val winningTeam: TextView = view.findViewById(R.id.winningTeam)
        val SpikeStatus: TextView = view.findViewById(R.id.SpikePlanted)

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

                    spinnerRounds.adapter = arrayAdapter
                    for (i in 0 until rounds.length()) {
                        val number = i + 1
                        arrayAdapter.add("Round $number")
                    }

                    spinnerRounds.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View,
                                position: Int,
                                id: Long
                            ) {
                                //val specificRound = rounds[position] as JSONObject
                                val details =
                                    handleSpecificRoundDetails(rounds[position] as JSONObject)

                                winningTeam.text = "Team Won:  ${details[0]}"
                                if (details[0] == "Red") {
                                    winningTeam.setTextColor(Color.parseColor("#f94555"))
                                } else {
                                    winningTeam.setTextColor((Color.parseColor("#18e4b7")))
                                }

                                SpikeStatus.text =
                                    "Spike Planted?: ${details[1]}\nSpike Defused?: ${details[2]}" +
                                            "\nPlanted at ${details[3]}, ${details[4]}\nSite ${details[5]}"
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                            }
                        }
                }

            } catch (e: Exception) {
                uiThread {
                    AlertDialog.Builder(requireActivity()).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }
    }

    private fun handleSpecificRoundDetails(specificRound: JSONObject): ArrayList<String> {
        val allDetails = ArrayList<String>()

        allDetails.add(specificRound["winning_team"].toString()) // TEAM WON
        if (specificRound["bomb_planted"].toString() == "true") {
            allDetails.add("Yes")
        } else {
            allDetails.add("No")
        } // WAS BOMB PLANTED
        if (specificRound["bomb_defused"].toString() == "true") {
            allDetails.add("Yes")
        } else {
            allDetails.add("No")
        } // WAS BOMB DEFUSED

        try {
            val plantInfo = specificRound["plant_events"] as JSONObject
            val lol = plantInfo["plant_location"] as JSONObject
            allDetails.add(lol.optString("x")) // X COORDINATE OF SPIKE
            allDetails.add(lol.optString("y")) // Y COORDINATE OF SPIKE

            allDetails.add(plantInfo["plant_side"].toString()) // WHICH SITE WAS IT?!!?
        } catch (e: Exception) {
            allDetails.add("null")
            allDetails.add("null")
            allDetails.add("none.")
        }

        return allDetails
    }

}