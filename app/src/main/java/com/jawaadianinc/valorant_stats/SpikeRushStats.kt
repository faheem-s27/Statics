package com.jawaadianinc.valorant_stats

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class SpikeRushStats : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_spike_rush_stats, container, false)
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val PlayerName: TextView = view.findViewById(R.id.playerName3)
        val PlayerImage: ImageView = view.findViewById(R.id.playerImageSpikeRush)
        val URLIMAGE = requireActivity().intent.extras!!.getString("URL")
        val Name = requireActivity().intent.extras!!.getString("RiotName")
        val ID = requireActivity().intent.extras!!.getString("RiotID")
        if (activity?.isInMultiWindowMode == true){
            PlayerImage.resize(100, 100)
        }
        else{
            PlayerImage.resize(300, 300)
        }

        Picasso
            .get()
            .load(URLIMAGE)
            .fit()
            .into(PlayerImage)
        PlayerName.text = Name

        val GetAccountLevelURL = "https://api.henrikdev.xyz/valorant/v1/account/$Name/$ID"

        doAsync {
            try {
                val text = URL(GetAccountLevelURL).readText()
                var data = JSONObject(text)
                data = data["data"] as JSONObject
                val account_level = data["account_level"].toString()
                val puuid = data["puuid"].toString()

                activity?.runOnUiThread {
                    PlayerName.text =
                        "$Name\nLevel: $account_level"
                }


            } catch (e: Exception) {
                uiThread {
                    AlertDialog.Builder(requireActivity()).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }

            try {
                val jsonText =
                    URL("https://api.tracker.gg/api/v2/valorant/standard/profile/riot/$Name%23$ID").readText()
                val jsonObject = JSONObject(jsonText)
                val compSegments = jsonObject.getJSONObject("data")
                    .getJSONArray("segments").getJSONObject(5)
                    .getJSONObject("stats")
                val total_time = compSegments.getJSONObject("timePlayed").getString("displayValue")

                val arrayList = ArrayList<String>()
                val listviewComp: ListView = view.findViewById(R.id.listViewSpikeRush)
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

                activity?.applicationContext?.let {
                    activity?.runOnUiThread {
                        if (activity?.isInMultiWindowMode == false){
                            PlayerName.text = PlayerName.text.toString() + "\nTotal Time: $total_time"
                        }
                        listviewComp.adapter = mAdapter
                        val small = jsonObject.getJSONObject("data")
                            .getJSONArray("segments").getJSONObject(5)
                            .getJSONObject("stats")

                        val statsComps: Array<String> = arrayOf(
                            "timePlayed",
                            "matchesPlayed",
                            "matchesWon",
                            "matchesLost",
                            "matchesWinPct",
                            "matchesDuration",
                            "roundsPlayed",
                            "roundsWon",
                            "roundsLost",
                            "roundsWinPct",
                            "roundsDuration",
                            "econRating",
                            "econRatingPerMatch",
                            "econRatingPerRound",
                            "score",
                            "scorePerMatch",
                            "scorePerRound",
                            "kills",
                            "killsPerRound",
                            "killsPerMatch",
                            "killsPerMinute",
                            "headshots",
                            "headshotsPerRound",
                            "headshotsPerMatch",
                            "headshotsPerMinute",
                            "headshotsPercentage",
                            "deaths",
                            "deathsPerRound",
                            "deathsPerMatch",
                            "deathsPerMinute",
                            "assists",
                            "assistsPerMatch",
                            "assistsPerRound",
                            "assistsPerMinute",
                            "kDRatio",
                            "kDARatio",
                            "kADRatio",
                            "damage",
                            "damagePerMatch",
                            "damagePerRound",
                            "damagePerMinute",
                            "damageReceived",
                            "plants",
                            "plantsPerMatch",
                            "plantsPerRound",
                            "defuses",
                            "defusesPerMatch",
                            "defusesPerRound",
                            "firstBloods",
                            "firstBloodsPerMatch",
                            "grenadeCasts",
                            "ability1Casts",
                            "ability2Casts",
                            "ultimateCasts",
                            "dealtHeadshots",
                            "dealtBodyshots",
                            "dealtLegshots",
                            "receivedHeadshots",
                            "receivedBodyshots",
                            "receivedLegshots",
                            "deathsFirst",
                            "deathsLast",
                            "mostKillsInMatch",
                            "mostKillsInRound",
                            "flawless",
                            "clutches",
                            "thrifty",
                            "aces",
                            "teamAces",
                            "attackKDRatio",
                            "attackKills",
                            "attackDeaths",
                            "attackAssists",
                            "attackRoundsPlayed",
                            "attackRoundsWon",
                            "attackRoundsLost",
                            "attackRoundsWinPct",
                            "defenseKDRatio",
                            "defenseKills",
                            "defenseDeaths",
                            "defenseAssists",
                            "defenseRoundsPlayed",
                            "defenseRoundsWon",
                            "defenseRoundsLost",
                            "defenseRoundsWinPct"
                        )

                        for (stat in statsComps) {
                            val displayName =
                                small.getJSONObject(stat).getString("displayName")
                            val displayValue =
                                small.getJSONObject(stat).getString("displayValue")
                            when {
                                small.getJSONObject(stat)
                                    .getString("displayCategory") == "Attack" -> {
                                    mAdapter.add("$displayName (Attack): $displayValue")
                                }
                                small.getJSONObject(stat)
                                    .getString("displayCategory") == "Defense" -> {
                                    mAdapter.add("$displayName (Defense): $displayValue")
                                }
                                else -> {
                                    mAdapter.add("$displayName: $displayValue")
                                }
                            }
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

    fun ImageView.resize(
        newWidth: Int = layoutParams.width, // pixels
        newHeight: Int = layoutParams.height // pixels
    ) {
        layoutParams.apply {
            width = newWidth // in pixels
            height = newHeight // in pixels
            layoutParams = this
        }
    }

}