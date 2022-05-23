package com.jawaadianinc.valorant_stats.valo.match_info

import android.annotation.SuppressLint
import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.math.roundToInt


@SuppressLint("SetTextI18n")
class RoundsMoreDetailsFragment : Fragment() {
    private var xMult: Double = 0.0
    private var yMult: Double = 0.0
    private var xScalar: Double = 0.0
    private var yScalar: Double = 0.0
    private var mapofPlayerandAgent: MutableMap<String, String> = mutableMapOf("player" to "agent")

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

        val defuseBT: Button = requireView().findViewById(R.id.defuseBT)
        val spinnerRounds: Spinner = view.findViewById(R.id.roundsSpinner)
        val minimapImage: ImageView = view.findViewById(R.id.mapImage)
        val mapName: TextView = view.findViewById(R.id.mapName)
        val display: Display = requireActivity().windowManager.defaultDisplay
        val width = display.width

        minimapImage.layoutParams?.height = width
        minimapImage.layoutParams?.width = width
        minimapImage.requestLayout()
        minimapImage.scaleType = ImageView.ScaleType.FIT_XY

        doAsync {
            try {
                val jsonDetails = MatchHistoryActivity.matchJSON
                val matchData = jsonDetails?.get("data") as JSONObject
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
                    JSONObject(URL("https://valorant-api.com/v1/maps/${mapUUID}").readText())["data"] as JSONObject
                xMult = mapCoordinates["xMultiplier"] as Double
                yMult = mapCoordinates["yMultiplier"] as Double
                xScalar = mapCoordinates["xScalarToAdd"] as Double
                yScalar = mapCoordinates["yScalarToAdd"] as Double
                val allPlayers =
                    matchData.getJSONObject("players").getJSONArray("all_players") as JSONArray
                for (i in 0 until allPlayers.length()) {
                    val data = allPlayers[i] as JSONObject
                    val playerName = data.getString("name")
                    val playerTag = data.getString("tag")
                    val agentURL =
                        data.getJSONObject("assets").getJSONObject("agent").getString("small")
                    val fullName = "$playerName#$playerTag"
                    mapofPlayerandAgent[fullName] = agentURL
                }

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
                                spikeStats.text =
                                    "Planted By: ${matchDetails[6]}" +
                                            ", ${matchDetails[0]} won"
                            }

                            override fun onNothingSelected(parent: AdapterView<*>) {
                            }
                        }

                    defuseBT.setOnClickListener {
                        val getRoundName =
                            spinnerRounds.selectedItem.toString()
                        val numberinRound = getRoundName.split(" ")
                        val actualRound: Int = numberinRound[1].toInt() - 1
                        handleDefusedMap(rounds[actualRound] as JSONObject)
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
        val defuseBT: Button = requireView().findViewById(R.id.defuseBT)
        val allDetails = ArrayList<String>()
        allDetails.add(specificRound["winning_team"].toString()) // TEAM WON
        if (specificRound["bomb_planted"].toString() == "true") {
            allDetails.add("Yes")
        } else {
            allDetails.add("No")
        } // WAS BOMB PLANTED
        if (specificRound["bomb_defused"] == true) {
            defuseBT.visibility = View.VISIBLE
            allDetails.add("Yes")
        } else {
            defuseBT.visibility = View.INVISIBLE
            allDetails.add("No")
        } // WAS BOMB DEFUSED

        try {
            val plantInfo = specificRound["plant_events"] as JSONObject
            val lol = plantInfo["plant_location"] as JSONObject
            allDetails.add(lol.optString("x")) // X COORDINATE OF SPIKE
            allDetails.add(lol.optString("y")) // Y COORDINATE OF SPIKE
            val playerPlant = plantInfo["planted_by"] as JSONObject
            allDetails.add(plantInfo["plant_site"].toString()) // WHICH SITE WAS IT?!!?
            allDetails.add(playerPlant.optString("display_name"))

            val spikePlanter = playerPlant.optString("display_name")
            val playerPosition: ImageView? = view?.findViewById(R.id.playerPos)
            val bitmap: Bitmap? = Bitmap.createBitmap(
                1024,
                1024,
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
                val name = requireActivity().intent.extras!!.getString("RiotName")
                val id = requireActivity().intent.extras!!.getString("RiotID")
                val riotName = "$name#$id"

                if (location["player_display_name"] == riotName) {
                    if (playerTeam == "Red") {
                        paint.color = Color.parseColor("#8B0000")
                    } else {
                        paint.color = Color.BLUE
                    }
                } else {
                    if (playerTeam == "Red") {
                        paint.color = Color.parseColor("#f94555")
                    } else {
                        paint.color = Color.parseColor("#18e4b7")
                    }
                }
                val finalX: Int = (((y * xMult) + xScalar) * 1024).roundToInt()
                val finalY: Int = (((x * yMult) + yScalar) * 1024).roundToInt()
                val SpikeIcon = BitmapFactory.decodeResource(
                    requireContext().resources,
                    R.drawable.spikelogo
                )

                //Draw Agent BitMaps
                val playerName = location["player_display_name"] as String
                val agentURL = mapofPlayerandAgent.getValue(playerName)
                val newIcon = Bitmap.createScaledBitmap(SpikeIcon, 60, 60, false)
                if (spikePlanter == location["player_display_name"]) {
                    if (playerTeam == "Red") {
                        paint.colorFilter = PorterDuffColorFilter(
                            Color.parseColor("#f94555"),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    } else {
                        paint.colorFilter = PorterDuffColorFilter(
                            Color.parseColor("#18e4b7"),
                            PorterDuff.Mode.SRC_ATOP
                        )
                    }
                    bitmap?.let { Canvas(it) }
                        ?.drawBitmap(newIcon, finalX.toFloat() - 30, finalY.toFloat() - 30, paint)
                } else {
                    Picasso.get().load(agentURL).into(object : com.squareup.picasso.Target {
                        override fun onBitmapLoaded(
                            playerBitMap: Bitmap?,
                            from: Picasso.LoadedFrom?
                        ) {
                            val resizedBitmap =
                                Bitmap.createScaledBitmap(playerBitMap!!, 50, 50, false)
                            if (playerTeam == "Red") {
                                paint.colorFilter = PorterDuffColorFilter(
                                    Color.parseColor("#f94555"),
                                    PorterDuff.Mode.DST_ATOP
                                )
                            } else {
                                paint.colorFilter = PorterDuffColorFilter(
                                    Color.parseColor("#18e4b7"),
                                    PorterDuff.Mode.DST_ATOP
                                )
                            }
                            bitmap?.let { Canvas(it) }
                                ?.drawBitmap(
                                    resizedBitmap!!,
                                    finalX.toFloat() - 25,
                                    finalY.toFloat() - 25,
                                    paint
                                )
                        }

                        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                    })
                }
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

    private fun handleDefusedMap(specificRound: JSONObject) {
        Toast.makeText(requireActivity(), "Coming soon!", Toast.LENGTH_SHORT).show()
    }
}
