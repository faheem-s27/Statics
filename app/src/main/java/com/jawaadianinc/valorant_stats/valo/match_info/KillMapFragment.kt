package com.jawaadianinc.valorant_stats.valo.match_info

import android.app.AlertDialog
import android.content.Intent
import android.content.res.Resources
import android.graphics.*
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.math.roundToInt

class KillMapFragment : Fragment() {
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
        return inflater.inflate(R.layout.fragment_kill_feed, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val minimapImage: ImageView = requireActivity().findViewById(R.id.killMapImage)
        val width = Resources.getSystem().displayMetrics.widthPixels

        minimapImage.layoutParams?.height = width
        minimapImage.layoutParams?.width = width
        minimapImage.requestLayout()
        minimapImage.scaleType = ImageView.ScaleType.FIT_XY

        //Log.d("MiniMap", "Width: $width, Height: $width")

        val checkBox: CheckBox = view.findViewById(R.id.imageCheckbox)

        try {
            val jsonDetails = MatchHistoryActivity.matchJSON
            val matchData = jsonDetails.get("data") as JSONObject
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
                        uiThread {
                            view.findViewById<TextView>(R.id.mapName2).text = map
                        }
                        break
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
                    Picasso.get().load(MatchHistoryActivity.mapURL).into(minimapImage)
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
                        view.findViewById(R.id.RoundSelectorKillsSpinner)

                    // add "All rounds" to the top of the spinner
                    arrayAdapter.add("All rounds")

                    val rounds: JSONArray =
                        jsonDetails.getJSONObject("data").getJSONArray("rounds")
                    for (i in 0 until rounds.length()) {
                        val roundNumnber = i + 1
                        arrayAdapter.add("Round $roundNumnber")
                    }

                    spinner.adapter = arrayAdapter
                    spinner.onItemSelectedListener =
                        object : AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: AdapterView<*>,
                                view: View,
                                position: Int,
                                id: Long
                            ) {

                                // get the index of the selected item
                                val selectedItemPosition = parent.selectedItemPosition - 1
                                if (selectedItemPosition == -1) {
                                    // all rounds
                                    handleAllRounds(checkBox.isChecked)
                                } else if (checkBox.isChecked) {
                                    handlePlayerImages(selectedItemPosition)
                                } else {
                                    handlePlayerDots(selectedItemPosition)
                                }

                                // sync spinners across all tabs
                                if (selectedItemPosition != -1) {
                                    activity!!.findViewById<Spinner>(R.id.roundSelect)
                                        .setSelection(selectedItemPosition)
                                }
                            }

                            override fun onNothingSelected(p0: AdapterView<*>?) {
                            }
                        }

                    checkBox.setOnCheckedChangeListener { _, isChecked ->
                        val getRoundName = spinner.selectedItemPosition - 1
                        if (getRoundName == -1) {
                            handleAllRounds(isChecked)
                        } else if (isChecked) {
                            handlePlayerImages(getRoundName)
                        } else {
                            handlePlayerDots(getRoundName)
                        }
                    }
                }
            }

        } catch (e: Exception) {
        }
    }

    private fun showBitMapFullScreen(bitmap: Bitmap) {
        val builder = AlertDialog.Builder(activity)
        val imageView = ImageView(activity)
        imageView.setImageBitmap(bitmap)
        builder.setView(imageView)
        builder.setNegativeButton("Close") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showBitMap(bitmap: Bitmap) {
        val intent = Intent(Intent.ACTION_VIEW)
        val uri = Uri.parse(
            MediaStore.Images.Media.insertImage(
                activity?.contentResolver,
                bitmap,
                null,
                null
            )
        )
        intent.setDataAndType(uri, "image/*")
        startActivity(intent)
    }

    private fun handlePlayerImages(roundNumber: Int) {
        val width = Resources.getSystem().displayMetrics.widthPixels
        // Scales the coordinates to the screen size (should be highly accurate now)
        val multiplier = (width.toFloat() / 1024f)

        val jsonDetails = MatchHistoryActivity.matchJSON
        val playerPosition: ImageView? = view?.findViewById(R.id.playerPos2)

        //

//        val bitmap: Bitmap? = Bitmap.createBitmap(
//            (1024 * multiplier).toInt(),
//            (1024 * multiplier).toInt(),
//            Bitmap.Config.ARGB_8888
//        )

        val minimapImage: ImageView = requireActivity().findViewById(R.id.killMapImage)
        minimapImage.visibility = View.INVISIBLE
        val minitMapBitmap = minimapImage.drawable.toBitmap()
        // make bitmap mutable
        val bitmap = minitMapBitmap.copy(Bitmap.Config.ARGB_8888, true)

        val rounds: JSONArray =
            jsonDetails.getJSONObject("data").getJSONArray("rounds")
        var total = 0
        val currentRound: JSONObject = rounds[roundNumber] as JSONObject
        val playerStats: JSONArray = currentRound.getJSONArray("player_stats")
        for (j in 0 until playerStats.length()) {
            val currentPlayer: JSONObject = playerStats.getJSONObject(j)
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
                var killerX = 0
                var killerY = 0
                var killerTeam = ""

                for (h in 0 until playerLocationsOnKillArray.length()) {
                    val player: JSONObject = playerLocationsOnKillArray[h] as JSONObject
                    val playerName = player.getString("player_display_name")
                    if (playerName == killerName) {
                        killerX = player.getJSONObject("location").getString("x").toInt()
                        killerY = player.getJSONObject("location").getString("y").toInt()
                        killerTeam = player.getString("player_team")
                    }
                }


//                val finalVictimX: Int =
//                    (((victimY.toInt() * xMult) + xScalar) * 1024 * multiplier).roundToInt()
//                val finalVictimY: Int =
//                    (((victimX.toInt() * yMult) + yScalar) * 1024 * multiplier).roundToInt()
//                val finalKillerX: Int =
//                    (((killerY * xMult) + xScalar) * 1024 * multiplier).roundToInt()
//                val finalKillerY: Int =
//                    (((killerX * yMult) + yScalar) * 1024 * multiplier).roundToInt()

                val finalVictimX: Int =
                    (((victimY.toInt() * xMult) + xScalar) * 1024).roundToInt()
                val finalVictimY: Int =
                    (((victimX.toInt() * yMult) + yScalar) * 1024).roundToInt()
                val finalKillerX: Int =
                    (((killerY * xMult) + xScalar) * 1024).roundToInt()
                val finalKillerY: Int =
                    (((killerX * yMult) + yScalar) * 1024).roundToInt()

                val killerAgentURL = mapofPlayerandAgent.getValue(killerName as String)
                val victimAgentURL = mapofPlayerandAgent.getValue(victimName as String)

                val paint = Paint()


                if (killerTeam == "Red") {
                    paint.color = Color.parseColor("#f94555")
                } else {
                    paint.color = Color.parseColor("#18e4b7")
                }

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

                paint.style = Paint.Style.FILL
                paint.strokeWidth = 8F
                paint.color = Color.BLACK

                Picasso.get().load(victimAgentURL).into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(
                        playerBitMap: Bitmap?,
                        from: Picasso.LoadedFrom?
                    ) {
                        val resizedBitmap =
                            Bitmap.createScaledBitmap(playerBitMap!!, 40, 40, false)
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
                                finalVictimX.toFloat() - 20,
                                finalVictimY.toFloat() - 20,
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
                            Bitmap.createScaledBitmap(playerBitMap!!, 40, 40, false)
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
                                finalKillerX.toFloat() - 20,
                                finalKillerY.toFloat() - 20,
                                paint
                            )
                    }

                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                    override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {}
                })


                total += 1

            }
        }

        playerPosition?.setImageBitmap(bitmap)
    }

    private fun handlePlayerDots(roundNumber: Int) {

        val width = Resources.getSystem().displayMetrics.widthPixels

        // Scales the coordinates to the screen size (should be highly accurate now)
        val multiplier = (width.toFloat() / 1024f)

        val jsonDetails = MatchHistoryActivity.matchJSON
        val playerPosition: ImageView? = view?.findViewById(R.id.playerPos2)
        val minimapImage: ImageView = requireActivity().findViewById(R.id.killMapImage)
        minimapImage.visibility = View.INVISIBLE
        val minitMapBitmap = minimapImage.drawable.toBitmap()
        // make bitmap mutable
        val bitmap = minitMapBitmap.copy(Bitmap.Config.ARGB_8888, true)

        playerPosition!!.setOnClickListener {
            showBitMap(bitmap)
        }

        val rounds: JSONArray =
            jsonDetails.getJSONObject("data").getJSONArray("rounds")
        var total = 0
        val currentRound: JSONObject = rounds[roundNumber] as JSONObject
        val playerStats: JSONArray = currentRound.getJSONArray("player_stats")
        for (j in 0 until playerStats.length()) {
            val currentPlayer: JSONObject = playerStats.getJSONObject(j)
            val killEvents: JSONArray = currentPlayer.getJSONArray("kill_events")
            for (k in 0 until killEvents.length()) {
                val currentKill = killEvents[k] as JSONObject
                val victimLocation = currentKill.getJSONObject("victim_death_location")
                val victimColour = currentKill.getString("victim_team") as String
                val victimX = victimLocation.getString("x")
                val victimY = victimLocation.getString("y")
                //val victimName = currentKill.getString("victim_display_name")

                //Get killer name and location
                val killerName = currentKill.get("killer_display_name")
                val playerLocationsOnKillArray: JSONArray =
                    currentKill.getJSONArray("player_locations_on_kill")
                var killerX = 0
                var killerY = 0
                var killerTeam = ""

                for (h in 0 until playerLocationsOnKillArray.length()) {
                    val player: JSONObject = playerLocationsOnKillArray[h] as JSONObject
                    val playerName = player.getString("player_display_name")
                    if (playerName == killerName) {
                        killerX = player.getJSONObject("location").getString("x").toInt()
                        killerY = player.getJSONObject("location").getString("y").toInt()
                        killerTeam = player.getString("player_team")
                    }
                }

                val finalVictimX: Int =
                    (((victimY.toInt() * xMult) + xScalar) * 1024).roundToInt()
                val finalVictimY: Int =
                    (((victimX.toInt() * yMult) + yScalar) * 1024).roundToInt()
                val finalKillerX: Int =
                    (((killerY * xMult) + xScalar) * 1024).roundToInt()
                val finalKillerY: Int =
                    (((killerX * yMult) + yScalar) * 1024).roundToInt()

                //Paint properties
                val paint = Paint()
                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 8F
                val radius = 6F
                paint.color = Color.BLACK

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

                paint.style = Paint.Style.FILL_AND_STROKE
                paint.strokeWidth = 8F
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

                total += 1

            }
        }

        playerPosition.setImageBitmap(bitmap)
    }

    private fun handleAllRounds(imagesEnabled: Boolean) {

        val width = Resources.getSystem().displayMetrics.widthPixels
        val minimapImage: ImageView = requireActivity().findViewById(R.id.killMapImage)
        minimapImage.visibility = View.VISIBLE

        // Scales the 3coordinates to the screen size (should be highly accurate now)
        val multiplier = (width.toFloat() / 1024f)

        val jsonDetails = MatchHistoryActivity.matchJSON
        val playerPosition: ImageView? = view?.findViewById(R.id.playerPos2)
        val bitmap: Bitmap? = Bitmap.createBitmap(
            (1024 * multiplier).roundToInt(),
            (1024 * multiplier).roundToInt(),
            Bitmap.Config.ARGB_8888
        )
        val rounds: JSONArray =
            jsonDetails.getJSONObject("data").getJSONArray("rounds")

        for (i in 0 until rounds.length()) {
            val playerStats: JSONArray = rounds.getJSONObject(i).getJSONArray("player_stats")
            for (j in 0 until playerStats.length()) {
                val currentPlayer: JSONObject = playerStats.getJSONObject(j)
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
                    var killerX = 0
                    var killerY = 0
                    var killerTeam = ""

                    for (h in 0 until playerLocationsOnKillArray.length()) {
                        val player: JSONObject = playerLocationsOnKillArray[h] as JSONObject
                        val playerName = player.getString("player_display_name")
                        if (playerName == killerName) {
                            killerX = player.getJSONObject("location").getString("x").toInt()
                            killerY = player.getJSONObject("location").getString("y").toInt()
                            killerTeam = player.getString("player_team")
                        }
                    }


                    val finalVictimX: Int =
                        (((victimY.toInt() * xMult) + xScalar) * 1024 * multiplier).roundToInt()
                    val finalVictimY: Int =
                        (((victimX.toInt() * yMult) + yScalar) * 1024 * multiplier).roundToInt()
                    val finalKillerX: Int =
                        (((killerY * xMult) + xScalar) * 1024 * multiplier).roundToInt()
                    val finalKillerY: Int =
                        (((killerX * yMult) + yScalar) * 1024 * multiplier).roundToInt()

                    if (imagesEnabled) {
                        // doing all kills with images
                        val killerAgentURL = mapofPlayerandAgent.getValue(killerName as String)
                        val victimAgentURL = mapofPlayerandAgent.getValue(victimName as String)

                        val paint = Paint()


                        if (killerTeam == "Red") {
                            paint.color = Color.parseColor("#f94555")
                        } else {
                            paint.color = Color.parseColor("#18e4b7")
                        }

                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 2F
                        bitmap?.let { Canvas(it) }
                            ?.drawLine(
                                finalKillerX.toFloat(),
                                finalKillerY.toFloat(),
                                finalVictimX.toFloat(),
                                finalVictimY.toFloat(),
                                paint
                            )

                        paint.style = Paint.Style.FILL
                        paint.strokeWidth = 6F
                        paint.color = Color.BLACK

                        Picasso.get().load(victimAgentURL)
                            .into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(
                                    playerBitMap: Bitmap?,
                                    from: Picasso.LoadedFrom?
                                ) {
                                    val resizedBitmap =
                                        Bitmap.createScaledBitmap(playerBitMap!!, 32, 32, false)
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
                                            finalVictimX.toFloat() - 16,
                                            finalVictimY.toFloat() - 16,
                                            paint
                                        )
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                                override fun onBitmapFailed(
                                    e: Exception?,
                                    errorDrawable: Drawable?
                                ) {
                                }
                            })

                        Picasso.get().load(killerAgentURL)
                            .into(object : com.squareup.picasso.Target {
                                override fun onBitmapLoaded(
                                    playerBitMap: Bitmap?,
                                    from: Picasso.LoadedFrom?
                                ) {
                                    val resizedBitmap =
                                        Bitmap.createScaledBitmap(playerBitMap!!, 32, 32, false)
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
                                            finalKillerX.toFloat() - 16,
                                            finalKillerY.toFloat() - 16,
                                            paint
                                        )
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                                override fun onBitmapFailed(
                                    e: Exception?,
                                    errorDrawable: Drawable?
                                ) {
                                }
                            })


                    } else {
                        // doing all kills without images
                        //Paint properties
                        val paint = Paint()



                        if (killerTeam == "Red") {
                            paint.color = Color.parseColor("#f94555")
                        } else {
                            paint.color = Color.parseColor("#18e4b7")
                        }


                        paint.style = Paint.Style.STROKE
                        paint.strokeWidth = 2F
                        bitmap?.let { Canvas(it) }
                            ?.drawLine(
                                finalKillerX.toFloat(),
                                finalKillerY.toFloat(),
                                finalVictimX.toFloat(),
                                finalVictimY.toFloat(),
                                paint
                            )

                        paint.style = Paint.Style.FILL_AND_STROKE
                        paint.strokeWidth = 6F
                        val radius = 4F
                        //paint.color = Color.BLACK

                        bitmap?.let { Canvas(it) }
                            ?.drawCircle(
                                finalKillerX.toFloat(),
                                finalKillerY.toFloat(),
                                radius, paint
                            )


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
                    }
                }
            }
        }
        playerPosition?.setImageBitmap(bitmap)
    }

}
