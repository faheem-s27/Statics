package com.jawaadianinc.valorant_stats.valorant

import android.graphics.*
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Display
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.math.roundToInt

class kill_map_Fragment : Fragment() {

    var xMult: Double = 0.0
    var yMult: Double = 0.0
    var xScalar: Double = 0.0
    var yScalar: Double = 0.0
    var mapofPlayerandAgent: MutableMap<String, String> = mutableMapOf("player" to "agent")

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_kill_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val name = requireActivity().intent.extras!!.getString("RiotName")
        val id = requireActivity().intent.extras!!.getString("RiotID")
        val minimapImage: ImageView = requireActivity().findViewById(R.id.killMapImage)
        val display: Display = requireActivity().windowManager.defaultDisplay
        val width = display.width

        minimapImage.layoutParams?.height = width
        minimapImage.layoutParams?.width = width
        minimapImage.requestLayout()
        minimapImage.scaleType = ImageView.ScaleType.FIT_XY

        val checkBox: CheckBox = view.findViewById(R.id.imageCheckbox)

        try {
            val jsonDetails = MatchHistoryActivity.matchJSON
            val matchData = jsonDetails?.get("data") as JSONObject
            val metadata = matchData.getJSONObject("metadata")
            val map = metadata.getString("map")
            var mapUUID = ""
            doAsync {
                val jsonOfMap = JSONObject(URL("https://valorant-api.com/v1/maps").readText())
                val mapData = jsonOfMap["data"] as JSONArray

                for (i in 0 until mapData.length()) {
                    val mapNamefromJSON = mapData[i] as JSONObject
                    val nameofMpa = mapNamefromJSON["displayName"]
                    if (nameofMpa == map) {
                        mapUUID = mapNamefromJSON["uuid"].toString()
                    }
                }
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
                    val spinner: Spinner =
                        view.findViewById<Spinner>(R.id.RoundSelectorKillsSpinner)
                    spinner.adapter = arrayAdapter

                    val rounds: JSONArray =
                        jsonDetails.getJSONObject("data").getJSONArray("rounds")
                    for (i in 0 until rounds.length()) {
                        val roundNumnber = i + 1
                        arrayAdapter.add("Round $roundNumnber")
                    }

                    spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View,
                                position: Int,
                                id: Long
                            ) {
                                val getRoundName =
                                    spinner.getItemAtPosition(position).toString()
                                val numberinRound = getRoundName.split(" ")
                                val actualRound: Int = numberinRound[1].toInt() - 1
                                if (checkBox.isChecked) {
                                    handlePlayerImages(actualRound)
                                } else {
                                    handlePlayerDots(actualRound)
                                }
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {
                            }
                        }

                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        val getRoundName =
                            spinner.selectedItem.toString()
                        val numberinRound = getRoundName.split(" ")
                        val actualRound: Int = numberinRound[1].toInt() - 1
                        if (isChecked) {
                            handlePlayerImages(actualRound)
                        } else {
                            handlePlayerDots(actualRound)
                        }
                    }
                }
            }

        } catch (e: Exception) {
            Log.d("test", e.toString())
        }
    }

    private fun handlePlayerImages(roundNumber: Int) {
        val jsonDetails = MatchHistoryActivity.matchJSON
        val playerPosition: ImageView? = view?.findViewById(R.id.playerPos2)
        val bitmap: Bitmap? = Bitmap.createBitmap(
            1000,
            1000,
            Bitmap.Config.ARGB_8888
        )
        val rounds: JSONArray =
            jsonDetails?.getJSONObject("data")!!.getJSONArray("rounds")
        var total = 0
        val currentRound: JSONObject = rounds[roundNumber] as JSONObject
        val player_stats: JSONArray = currentRound.getJSONArray("player_stats")
        for (j in 0 until player_stats.length()) {
            val currentPlayer: JSONObject = player_stats.getJSONObject(j)
            val killEvents: JSONArray = currentPlayer.getJSONArray("kill_events")
            for (k in 0 until killEvents.length()) {
                val currentKill = killEvents[k] as JSONObject
                val victimLocation = currentKill.getJSONObject("victim_death_location")
                val victimColour = currentKill.getString("victim_team") as String
                val victimX = victimLocation.getString("x")
                val victimY = victimLocation.getString("y")
                val victimName = currentKill.getString("victim_display_name")

                //Get killer name and location
                val killerName = currentKill.get("killer_display_name")
                val playerLocationsOnKillArray: JSONArray =
                    currentKill.getJSONArray("player_locations_on_kill")
                var killerX: Int = 0
                var killerY: Int = 0
                var killerTeam: String = ""

                for (h in 0 until playerLocationsOnKillArray.length()) {
                    val player: JSONObject = playerLocationsOnKillArray[h] as JSONObject
                    val playerName = player.getString("player_display_name")
                    if (playerName == killerName) {
                        killerX = player.getJSONObject("location").getString("x").toInt()
                        killerY = player.getJSONObject("location").getString("y").toInt()
                        killerTeam = player.getString("player_team")
                    }
                }

                val finalVictimX: Int = (((victimY.toInt() * xMult) + xScalar) * 1000).roundToInt()
                val finalVictimY: Int = (((victimX.toInt() * yMult) + yScalar) * 1000).roundToInt()
                val finalKillerX: Int = (((killerY.toInt() * xMult) + xScalar) * 1000).roundToInt()
                val finalKillerY: Int = (((killerX.toInt() * yMult) + yScalar) * 1000).roundToInt()

                val killerAgentURL = mapofPlayerandAgent.getValue(killerName as String)
                val victimAgentURL = mapofPlayerandAgent.getValue(victimName as String)

                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.strokeWidth = 10F
                val radius: Float = 10.0F
                paint.color = Color.BLACK

                Picasso.get().load(victimAgentURL).into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(
                        playerBitMap: Bitmap?,
                        from: Picasso.LoadedFrom?
                    ) {
                        val resizedBitmap =
                            Bitmap.createScaledBitmap(playerBitMap!!, 50, 50, false)
                        if (victimColour == "Red") {
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
                                finalVictimX.toFloat() - 25,
                                finalVictimY.toFloat() - 25,
                                paint
                            )
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })

                Picasso.get().load(killerAgentURL).into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(
                        playerBitMap: Bitmap?,
                        from: Picasso.LoadedFrom?
                    ) {
                        val resizedBitmap =
                            Bitmap.createScaledBitmap(playerBitMap!!, 50, 50, false)
                        if (killerTeam == "Red") {
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
                                finalKillerX.toFloat() - 25,
                                finalKillerY.toFloat() - 25,
                                paint
                            )
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })

                if (killerTeam == "Red") {
                    paint.color = Color.parseColor("#f94555")
                } else {
                    paint.color = Color.parseColor("#18e4b7")
                }

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 1F
                bitmap?.let { Canvas(it) }
                    ?.drawLine(
                        finalKillerX.toFloat(),
                        finalKillerY.toFloat(),
                        finalVictimX.toFloat(),
                        finalVictimY.toFloat(),
                        paint
                    )
                total += 1

            }
        }

        playerPosition?.setImageBitmap(bitmap)
    }

    private fun handlePlayerDots(roundNumber: Int) {
        val jsonDetails = MatchHistoryActivity.matchJSON
        val playerPosition: ImageView? = view?.findViewById(R.id.playerPos2)
        val bitmap: Bitmap? = Bitmap.createBitmap(
            1000,
            1000,
            Bitmap.Config.ARGB_8888
        )
        val rounds: JSONArray =
            jsonDetails?.getJSONObject("data")!!.getJSONArray("rounds")
        var total = 0
        val currentRound: JSONObject = rounds[roundNumber] as JSONObject
        val player_stats: JSONArray = currentRound.getJSONArray("player_stats")
        for (j in 0 until player_stats.length()) {
            val currentPlayer: JSONObject = player_stats.getJSONObject(j)
            val killEvents: JSONArray = currentPlayer.getJSONArray("kill_events")
            for (k in 0 until killEvents.length()) {
                val currentKill = killEvents[k] as JSONObject
                val victimLocation = currentKill.getJSONObject("victim_death_location")
                val victimColour = currentKill.getString("victim_team") as String
                val victimX = victimLocation.getString("x")
                val victimY = victimLocation.getString("y")
                val victimName = currentKill.getString("victim_display_name")

                //Get killer name and location
                val killerName = currentKill.get("killer_display_name")
                val playerLocationsOnKillArray: JSONArray =
                    currentKill.getJSONArray("player_locations_on_kill")
                var killerX: Int = 0
                var killerY: Int = 0
                var killerTeam: String = ""

                for (h in 0 until playerLocationsOnKillArray.length()) {
                    val player: JSONObject = playerLocationsOnKillArray[h] as JSONObject
                    val playerName = player.getString("player_display_name")
                    if (playerName == killerName) {
                        killerX = player.getJSONObject("location").getString("x").toInt()
                        killerY = player.getJSONObject("location").getString("y").toInt()
                        killerTeam = player.getString("player_team")
                        //Log.d("kills", "Round $i - Victim Name: $victimName - Killer Name: $playerName")
                    }
                }

                val finalVictimX: Int = (((victimY.toInt() * xMult) + xScalar) * 1000).roundToInt()
                val finalVictimY: Int = (((victimX.toInt() * yMult) + yScalar) * 1000).roundToInt()
                val finalKillerX: Int = (((killerY.toInt() * xMult) + xScalar) * 1000).roundToInt()
                val finalKillerY: Int = (((killerX.toInt() * yMult) + yScalar) * 1000).roundToInt()

                //Paint properties
                val paint = Paint()
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 10F
                val radius: Float = 10.0F
                paint.color = Color.BLACK

                if (victimColour == "Red") {
                    paint.color = Color.parseColor("#f94555")
                } else {
                    paint.color = Color.parseColor("#18e4b7")
                }

                bitmap?.let { Canvas(it) }
                    ?.drawCircle(
                        finalVictimX.toFloat(),
                        finalVictimY.toFloat(),
                        radius, paint
                    )

                if (killerTeam == "Red") {
                    paint.color = Color.parseColor("#f94555")
                } else {
                    paint.color = Color.parseColor("#18e4b7")
                }

                bitmap?.let { Canvas(it) }
                    ?.drawCircle(
                        finalKillerX.toFloat(),
                        finalKillerY.toFloat(),
                        radius, paint
                    )

                paint.style = Paint.Style.STROKE
                paint.strokeWidth = 3F
                bitmap?.let { Canvas(it) }
                    ?.drawLine(
                        finalKillerX.toFloat(),
                        finalKillerY.toFloat(),
                        finalVictimX.toFloat(),
                        finalVictimY.toFloat(),
                        paint
                    )
                total += 1

            }
        }

        playerPosition?.setImageBitmap(bitmap)
        Log.d("kills", "Total kills: $total")
    }
}
