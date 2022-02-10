package com.jawaadianinc.valorant_stats.valorant

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.io.FileNotFoundException
import java.net.URL


class CompetitiveStats : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_competitive_stats, container, false)
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val progress: ProgressBar = view.findViewById(R.id.progress2)
        val playerName: TextView = view.findViewById(R.id.PlayerName)
        val playerImage: ImageView = view.findViewById(R.id.PlayerImageComp)
        val urlImage = requireActivity().intent.extras!!.getString("URL")
        val name = requireActivity().intent.extras!!.getString("RiotName")
        val id = requireActivity().intent.extras!!.getString("RiotID")

        if (activity?.isInMultiWindowMode == true) {
            playerImage.resize(100, 100)
        } else {
            playerImage.resize(300, 300)
        }

        playerImage.scaleType = ImageView.ScaleType.FIT_XY;

        Picasso
            .get()
            .load(urlImage)
            .fit()
            .into(playerImage)
        playerName.text = "Name: $name#$id"

        val progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Fetching Data")
        progressDialog.setMessage("Please wait a moment")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) // There are 3 styles, You'll figure it out :)
        progressDialog.setCancelable(false)

        //progressDialog.show()

        val getAccountDetails = "https://api.henrikdev.xyz/valorant/v1/account/$name/$id"

        doAsync {
            try {
                var text = URL(getAccountDetails).readText()
                var data = JSONObject(text)
                data = data["data"] as JSONObject
                val accountLevel = data["account_level"].toString()
                val puuid = data["puuid"].toString()

                activity?.runOnUiThread {
                    playerName.text =
                        "Name: $name#$id\nAccount Level: $accountLevel\nGetting Ranked Info..."
                }

                try {
                    text =
                        URL("https://api.henrikdev.xyz/valorant/v1/by-puuid/mmr/eu/$puuid").readText()
                    data = JSONObject(text)
                    data = data["data"] as JSONObject
                    val currentTier = data["currenttierpatched"].toString()
                    activity?.runOnUiThread {
                        if (activity?.isInMultiWindowMode == true) {
                            playerName.text = "$name\nLevel: $accountLevel"
                        } else {
                            playerName.text =
                                "$name\nLevel: $accountLevel\nRank: $currentTier"
                        }
                    }
                } catch (e: Exception) {
                    activity?.runOnUiThread {
                        if (activity?.isInMultiWindowMode == true){
                            playerName.text = "$name\nLevel: $accountLevel"
                        } else {
                            playerName.text =
                                "$name\nLevel: $accountLevel\nRank: Unranked"
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

            try {
                val jsonObject =
                    JSONObject(URL("https://api.tracker.gg/api/v2/valorant/standard/profile/riot/$name%23$id").readText())
                val compSegments = jsonObject.getJSONObject("data")
                    .getJSONArray("segments").getJSONObject(0)
                    .getJSONObject("stats")
                val totalTime = compSegments.getJSONObject("timePlayed").getString("displayValue")

                val arrayList = ArrayList<String>()
                val listviewComp : ListView = view.findViewById(R.id.listViewSpikeRush)
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
                            playerName.text =
                                playerName.text.toString() + "\nTotal Time: $totalTime"
                        }
                        listviewComp.adapter = mAdapter
                        val small = jsonObject.getJSONObject("data")
                            .getJSONArray("segments").getJSONObject(0)
                            .getJSONObject("stats")

                        val statsComps: Array<String> = arrayOf(
                            "timePlayed",
                            "matchesPlayed",
                            "matchesWon",
                            "matchesLost" ,
                            "matchesWinPct" ,
                            "matchesDuration" ,
                            "roundsPlayed" ,
                            "roundsWon" ,
                            "roundsLost" ,
                            "roundsWinPct" ,
                            "roundsDuration" ,
                            "econRating" ,
                            "econRatingPerMatch" ,
                            "econRatingPerRound" ,
                            "score" ,
                            "scorePerMatch" ,
                            "scorePerRound" ,
                            "kills" ,
                            "killsPerRound" ,
                            "killsPerMatch" ,
                            "killsPerMinute" ,
                            "headshots" ,
                            "headshotsPerRound" ,
                            "headshotsPerMatch" ,
                            "headshotsPerMinute" ,
                            "headshotsPercentage" ,
                            "deaths" ,
                            "deathsPerRound" ,
                            "deathsPerMatch" ,
                            "deathsPerMinute" ,
                            "assists" ,
                            "assistsPerMatch" ,
                            "assistsPerRound" ,
                            "assistsPerMinute" ,
                            "kDRatio" ,
                            "kDARatio" ,
                            "kADRatio" ,
                            "damage" ,
                            "damagePerMatch" ,
                            "damagePerRound" ,
                            "damagePerMinute" ,
                            "damageReceived" ,
                            "plants" ,
                            "plantsPerMatch" ,
                            "plantsPerRound" ,
                            "defuses" ,
                            "defusesPerMatch" ,
                            "defusesPerRound" ,
                            "firstBloods" ,
                            "firstBloodsPerMatch" ,
                            "grenadeCasts" ,
                            "ability1Casts" ,
                            "ability2Casts" ,
                            "ultimateCasts" ,
                            "dealtHeadshots" ,
                            "dealtBodyshots" ,
                            "dealtLegshots" ,
                            "receivedHeadshots" ,
                            "receivedBodyshots" ,
                            "receivedLegshots" ,
                            "deathsFirst",
                            "deathsLast" ,
                            "mostKillsInMatch" ,
                            "mostKillsInRound" ,
                            "flawless" ,
                            "clutches" ,
                            "thrifty" ,
                            "aces" ,
                            "teamAces" ,
                            "attackKDRatio" ,
                            "attackKills" ,
                            "attackDeaths" ,
                            "attackAssists" ,
                            "attackRoundsPlayed" ,
                            "attackRoundsWon" ,
                            "attackRoundsLost" ,
                            "attackRoundsWinPct" ,
                            "defenseKDRatio" ,
                            "defenseKills" ,
                            "defenseDeaths" ,
                            "defenseAssists" ,
                            "defenseRoundsPlayed" ,
                            "defenseRoundsWon" ,
                            "defenseRoundsLost",
                            "defenseRoundsWinPct",
                            "rank",
                            "peakRank")

                        for (stat in statsComps) {
                            try {
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
                            } catch (e: Exception) {

                            }
                        }
                        progress.visibility = View.INVISIBLE
                    }
                }

            } catch (e: FileNotFoundException) {
                uiThread {
                    AlertDialog.Builder(requireActivity()).setTitle("Could not be fetched!")
                        .setMessage("Server unavailable, try again later :/")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
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

    private fun ImageView.resize(
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
