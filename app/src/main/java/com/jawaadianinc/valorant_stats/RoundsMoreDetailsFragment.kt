package com.jawaadianinc.valorant_stats

import android.annotation.SuppressLint
import android.graphics.*
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.math.roundToInt


@SuppressLint("SetTextI18n")
class RoundsMoreDetailsFragment : Fragment() {

    var xMult: Double = 0.0
    var yMult: Double = 0.0
    var xScalar: Double = 0.0
    var yScalar: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_rounds_more_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val name = requireActivity().intent.extras!!.getString("RiotName")
        val id = requireActivity().intent.extras!!.getString("RiotID")
        val matchnumber = requireActivity().intent.extras!!.getInt("MatchNumber")

        val spinnerRounds: Spinner = view.findViewById(R.id.roundsSpinner)
        val minimapImage: ImageView = view.findViewById(R.id.mapImage)
        val mapName: TextView = view.findViewById(R.id.mapName)
        val coordinates: TextView = view.findViewById(R.id.spikeCoordinates)
        val spikePlanted: ImageView? = view.findViewById(R.id.SpikePlanted)
        val playerPosition: ImageView? = view.findViewById(R.id.playerPos)

        minimapImage.layoutParams?.height = 1000
        minimapImage.layoutParams?.width = 1000
        minimapImage.requestLayout()

        val IDofMatch = requireActivity().intent.extras!!.getString("MatchID")
        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/eu/${name}/$id?size=10"

        doAsync {
            try {
                var matchID: String = ""
                matchID = if (IDofMatch == "none") {
                    val matchhistoryURL = URL(allmatches).readText()
                    val jsonMatches = JSONObject(matchhistoryURL)
                    val data = jsonMatches["data"] as JSONArray
                    val easier = data.getJSONObject(matchnumber).getJSONObject("metadata")
                    easier.getString("matchid")
                } else {
                    IDofMatch!!
                }
                val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"

                val matchdetailsURL = URL(matchURl).readText()
                val jsonDetails = JSONObject(matchdetailsURL)
                val matchData = jsonDetails["data"] as JSONObject
                val metadata = matchData.getJSONObject("metadata")
                val map = metadata.getString("map")
                val rounds = matchData.getJSONArray("rounds")

                var mapUUID = ""

                val jsonOfMap = JSONObject(URL("https://valorant-api.com/v1/maps").readText())
                val mapData = jsonOfMap["data"] as JSONArray

                for (i in 0 until mapData.length()) {
                    val mapNamefromJSON = mapData[i] as JSONObject
                    val nameofMpa = mapNamefromJSON["displayName"]
                    if (nameofMpa == map) {
                        mapUUID = mapNamefromJSON["uuid"].toString()
                    }
                }

                val spikeStats: TextView = view.findViewById(R.id.SpikeStats)
                val mapCoordinates =
                    JSONObject(URL("https://valorant-api.com/v1/maps/${mapUUID}").readText())
                val details = mapCoordinates["data"] as JSONObject
                xMult = details["xMultiplier"] as Double
                yMult = details["yMultiplier"] as Double
                xScalar = details["xScalarToAdd"] as Double
                yScalar = details["yScalarToAdd"] as Double

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
                    mapName.text = map


                    when (map) {
                        "Bind" -> {
                            minimapImage.setImageResource(R.drawable.bind_minimap)
                        }
                        "Ascent" -> {
                            minimapImage.setImageResource(R.drawable.ascent_minimap)
                        }
                        "Split" -> {
                            minimapImage.setImageResource(R.drawable.split_minimap)
                        }
                        "Fracture" -> {
                            minimapImage.setImageResource(R.drawable.fracture_minimap)
                        }
                        "Breeze" -> {
                            minimapImage.setImageResource(R.drawable.breeze_minimap)
                        }
                        "Haven" -> {
                            minimapImage.setImageResource(R.drawable.haven_minimap)
                        }
                        "Icebox" -> {
                            minimapImage.setImageResource(R.drawable.icebox_minimap)
                        }
                    }

                    spinnerRounds.adapter = arrayAdapter
                    for (i in 0 until rounds.length()) {
                        val number = i + 1
                        val dataofSpike = rounds[i] as JSONObject
                        val wasSpikePlanted = dataofSpike["bomb_planted"].toString()
                        if (wasSpikePlanted == "true") {
                            arrayAdapter.add("Round $number")
                        }
                    }

                    spinnerRounds.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View,
                                position: Int,
                                id: Long
                            ) {
                                val getRoundName =
                                    spinnerRounds.getItemAtPosition(position).toString()
                                val numberinRound = getRoundName.split(" ")
                                val actualRound: Int = numberinRound[1].toInt() - 1
                                val matchDetails =
                                    handleSpecificRoundDetails(rounds[actualRound] as JSONObject)

                                if (matchDetails[0] == "Red") {
                                    spikeStats.setTextColor(Color.parseColor("#f94555"))
                                } else {
                                    spikeStats.setTextColor(Color.parseColor("#18e4b7"))
                                }

                                spikeStats.text = "Was Spike Planted? : ${matchDetails[1]}" +
                                        "\nWas Spike Defused? : ${matchDetails[2]}"

                                if (matchDetails[5] != "null") {
                                    spikeStats.append(
                                        "\nPlanted By: ${matchDetails[6]}" +
                                                "\nSite ${matchDetails[5]}"
                                    )
                                }
                                handleSpikeCoordinates(
                                    matchDetails[3].toInt(), matchDetails[4].toInt(),
                                    xMult, yMult, xScalar, yScalar
                                )
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
            val playerPlant = plantInfo["planted_by"] as JSONObject
            allDetails.add(plantInfo["plant_side"].toString()) // WHICH SITE WAS IT?!!?
            allDetails.add(playerPlant.optString("display_name"))
            val minimapImage: ImageView? = view?.findViewById(R.id.mapImage)
            val playerPosition: ImageView? = view?.findViewById(R.id.playerPos)
            val bitmap: Bitmap? = Bitmap.createBitmap(
                minimapImage!!.width,
                minimapImage.height,
                Bitmap.Config.ARGB_8888
            )

            val playerLocations = plantInfo["player_locations_on_plant"] as JSONArray

            for (i in 0 until playerLocations.length()) {
                val location = playerLocations[i] as JSONObject
                val actuallocation = location["location"] as JSONObject
                val x = actuallocation["x"] as Int
                val y = actuallocation["y"] as Int
                val playerTeam = location["player_team"] as String
                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.strokeWidth = 10F
                val radius: Int
                val name = requireActivity().intent.extras!!.getString("RiotName")
                val id = requireActivity().intent.extras!!.getString("RiotID")
                val riotName = "$name#$id"

                if (location["player_display_name"] == riotName) {
                    radius = 17
                    if (playerTeam == "Red") {
                        paint.color = Color.RED
                    } else {
                        paint.color = Color.BLUE
                    }
                } else {
                    radius = 15
                    if (playerTeam == "Red") {
                        paint.color = Color.parseColor("#f94555")
                    } else {
                        paint.color = Color.parseColor("#18e4b7")
                    }
                }
                val finalX: Int = (((y * xMult) + xScalar) * 1000).roundToInt()
                val finalY: Int = (((x * yMult) + yScalar) * 1000).roundToInt()

                bitmap?.let { Canvas(it) }
                    ?.drawCircle(finalX.toFloat(), finalY.toFloat(), radius.toFloat(), paint)
            }

            playerPosition?.setImageBitmap(bitmap)


        } catch (e: Exception) {
            allDetails.add(null.toString())
            allDetails.add(null.toString())
            allDetails.add(null.toString())
            allDetails.add(null.toString())
        }

        return allDetails
    }

    private fun handleSpikeCoordinates(
        x: Int,
        y: Int,
        xMultiplier: Double,
        yMultiplier: Double,
        xScalarToAdd: Double,
        yScalarToAdd: Double
    ) {
        val minimapImage: ImageView? = view?.findViewById(R.id.mapImage)
        val coordinates: TextView? = view?.findViewById(R.id.spikeCoordinates)
        val finalX: Int = (((y * xMultiplier) + xScalarToAdd) * 1000).roundToInt()
        val finalY: Int = (((x * yMultiplier) + yScalarToAdd) * 1000).roundToInt()
        coordinates?.text = "Spike planted at: $x, $y"

        val spikePlanted: ImageView? = view?.findViewById(R.id.SpikePlanted)
        val paint = Paint()
        val radius = 17
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 15F
        paint.color = Color.BLACK
        val bitmap =
            minimapImage.let {
                minimapImage?.width?.let { it1 ->
                    Bitmap.createBitmap(
                        it1,
                        minimapImage.height,
                        Bitmap.Config.ARGB_8888
                    )
                }
            }
        bitmap?.let { Canvas(it) }
            ?.drawCircle(finalX.toFloat(), finalY.toFloat(), radius.toFloat(), paint)
        spikePlanted?.setImageBitmap(bitmap)
    }


}