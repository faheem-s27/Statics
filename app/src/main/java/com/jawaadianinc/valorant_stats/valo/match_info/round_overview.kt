package com.jawaadianinc.valorant_stats.valo.match_info

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
import com.jawaadianinc.valorant_stats.valo.classes.eventsClass
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import kotlin.math.roundToInt


class round_overview : Fragment() {
    private var xMult: Double = 0.0
    private var yMult: Double = 0.0
    private var xScalar: Double = 0.0
    private var yScalar: Double = 0.0
    private var mapofPlayerandAgent: MutableMap<String, String> = mutableMapOf("player" to "agent")

    val jsonDetails = MatchHistoryActivity.matchJSON


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_round_overview, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val events = ArrayList<eventsClass>()


        val progressBar = ProgressBar(activity)
        progressBar.isIndeterminate = true
        progressBar.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val minimapImage: ImageView = requireActivity().findViewById(R.id.mapImageRoundOverview)
        val display: Display = requireActivity().windowManager.defaultDisplay
        val width = display.width
        minimapImage.layoutParams?.height = width
        minimapImage.layoutParams?.width = width
        minimapImage.requestLayout()
        minimapImage.scaleType = ImageView.ScaleType.FIT_XY
        try {
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
                            //view.findViewById<TextView>(R.id.roundDetailsText).text = map
                            Picasso.get().load(MatchHistoryActivity.mapURL).into(minimapImage)
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

                val redPlayers =
                    matchData.getJSONObject("players").getJSONArray("red") as JSONArray
                for (i in 0 until redPlayers.length()) {
                    val data = redPlayers[i] as JSONObject
                    val playerName = data.getString("name")
                    val playerTag = data.getString("tag")
                    val agentURL =
                        data.getJSONObject("assets").getJSONObject("agent").getString("small")
                    val fullName = "$playerName#$playerTag"
                    mapofPlayerandAgent[fullName] = agentURL
                }

                // remove the first item from the map
                mapofPlayerandAgent.remove("player")

                val bluePlayers =
                    matchData.getJSONObject("players").getJSONArray("blue") as JSONArray
                for (i in 0 until bluePlayers.length()) {
                    val data = bluePlayers[i] as JSONObject
                    val playerName = data.getString("name")
                    val playerTag = data.getString("tag")
                    val agentURL =
                        data.getJSONObject("assets").getJSONObject("agent").getString("small")
                    val fullName = "$playerName#$playerTag"
                    mapofPlayerandAgent[fullName] = agentURL
                }

                showEachPlayer(mapofPlayerandAgent)

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

                    val rounds: JSONArray =
                        jsonDetails.getJSONObject("data").getJSONArray("rounds")
                    for (i in 0 until rounds.length()) {
                        val roundNumnber = i + 1
                        arrayAdapter.add("Round $roundNumnber")
                    }

//                    spinner.adapter = arrayAdapter
//                    spinner.onItemSelectedListener =
//                        object : AdapterView.OnItemSelectedListener {
//                            override fun onItemSelected(
//                                parent: AdapterView<*>,
//                                view: View,
//                                position: Int,
//                                id: Long
//                            ) {
//
//                                // get current index of spinner
//                                val currentIndex = spinner.selectedItemPosition
//                                loadData(currentIndex)
//
//                            }
//
//                            override fun onNothingSelected(p0: AdapterView<*>?) {
//                            }
//                        }
//
                }
            }
        } catch (e: Exception) {
            // show alert dialog to user that an error occured while trying to get the match data
            AlertDialog.Builder(requireContext())
                .setTitle("Error")
                .setMessage("An error occured while trying to get the match data. Please try again later.")
                .setPositiveButton("OK") { dialog, which ->
                    dialog.dismiss()
                }
                .show()
        }

    }

    private fun showEachPlayer(player_agentURL: MutableMap<String, String>) {
        try {
            // inflate the layout for each player
            // show the player name and the agent image by inflating a textview and an imageview
            val playerImage = requireActivity().findViewById<ImageView>(R.id.agentImageOV)
            val playerName = requireActivity().findViewById<TextView>(R.id.playerNameOV)

            // a collection of views
            val views = ArrayList<View>()
            views.add(requireView().findViewById(R.id.player1))
            views.add(requireView().findViewById(R.id.player2))
            views.add(requireView().findViewById(R.id.player3))
            views.add(requireView().findViewById(R.id.player4))
            views.add(requireView().findViewById(R.id.player5))
            views.add(requireView().findViewById(R.id.player6))
            views.add(requireView().findViewById(R.id.player7))
            views.add(requireView().findViewById(R.id.player8))
            views.add(requireView().findViewById(R.id.player9))
            views.add(requireView().findViewById(R.id.player10))

            doAsync {
                uiThread {
                    playerName.text = "These were the players in the match:"
                }
                Thread.sleep(2000)
                for ((key, value) in player_agentURL) {
                    val playerNameText = key.split("#")[0]
                    // get the value from key
                    val agentURL = value
                    // get the current index of the for loop
                    val currentIndex = player_agentURL.keys.indexOf(key)
                    val view = views[currentIndex] as ImageView
                    uiThread {
                        // picasso on bitmap loaded
                        Picasso.get().load(agentURL).into(object : com.squareup.picasso.Target {
                            override fun onBitmapLoaded(
                                bitmap: Bitmap?,
                                from: Picasso.LoadedFrom?
                            ) {
                                view.alpha = 0.0f
                                playerImage.alpha = 0f
                                // load the bitmap into the imageview
                                playerImage.setImageBitmap(bitmap)
                                view.setImageBitmap(bitmap)
                                playerName.setTextAnimation(playerNameText)
                                // animate the player name to the view position and resizing the image size and then at the end, return the player image to its original position
                                playerImage.animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .start()

                                // fade in the view
                                view.animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .start()

                                if (currentIndex > 4) {
                                    // set background colour to Valorant Red
                                    view.setBackgroundColor(Color.parseColor("#18e4b7"))
                                } else {
                                    view.setBackgroundColor(Color.parseColor("#f94555"))

                                }
                            }

                            override fun onBitmapFailed(
                                e: java.lang.Exception?,
                                errorDrawable: Drawable?
                            ) {
                                //TODO("Not yet implemented")
                            }

                            override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                //TODO("Not yet implemented")
                            }

                        })
                    }
                    Thread.sleep(1000)
                }

                // hide the player name and the player image
                uiThread {
                    playerName.visibility = View.GONE
                    playerImage.visibility = View.GONE
                    mapTime()
                }

            }
        } catch (e: Exception) {
        }
    }

    private fun mapTime() {
        try {
            val mapImage = requireActivity().findViewById<ImageView>(R.id.mapImageRoundOverview)
            // make it visible
            mapImage.alpha = 0f
            mapImage.visibility = View.VISIBLE
            // make the map image twice as big without animating it
            mapImage.layoutParams.width = mapImage.layoutParams.width * 2
            mapImage.layoutParams.height = mapImage.layoutParams.height * 2
            mapImage.requestLayout()

            mapImage.animate()
                .alpha(1f)
                .scaleX(1 / 2f)
                .scaleY(1 / 2f)
                .setDuration(500)
                .start()

            val rounds: JSONArray =
                jsonDetails.getJSONObject("data").getJSONArray("rounds")
            doAsync {
                for (i in 0 until rounds.length()) {
                    uiThread {
                        try {
                            startRoundData(i)
                        } catch (e: Exception) {
                            // toast to user that an error occured while trying to get the match data
                            //Toast.makeText(requireContext(), "An error occured while trying to get the match data. Please try again later.", Toast.LENGTH_LONG).show()
                        }
                    }
                    Thread.sleep(3000)
                }
            }
        } catch (e: Exception) {
        }
    }

    private fun loadData(roundNumber: Int) {
        // toast message to show the user that the data is loading
        Toast.makeText(activity?.applicationContext, "Loading data...", Toast.LENGTH_SHORT).show()

    }

    private fun startRoundData(roundNumber: Int) {
        val mapImage = requireActivity().findViewById<ImageView>(R.id.mapImageRoundOverview)
        val roundText = requireActivity().findViewById<TextView>(R.id.roundDetailsText)

        // make the map image 50% opacity via animation
        mapImage.animate()
            .alpha(0.3f)
            .setDuration(500)
            .withEndAction {
                // make the map image fully opaque via animation
                mapImage.animate()
                    .alpha(1f)
                    .setDuration(500)
                    .startDelay = 1000
            }
            .start()

        // set the round text to the round number
        roundText.text = "Round " + (roundNumber + 1).toString()

        // animate the round text to the middle of the screen
        roundText.animate()
            .alpha(1f)
            .setDuration(500)
            .withEndAction {
                // animate the text to fade out
                roundText.animate()
                    .alpha(0f)
                    .setDuration(500)
                    .startDelay = 1000
            }
            .start()

        val display: Display = requireActivity().windowManager.defaultDisplay
        val width = display.width

        // Scales the coordinates to the screen size (should be highly accurate now)
        val multiplier = (width.toFloat() / 1024f)

        val jsonDetails = MatchHistoryActivity.matchJSON
        val playerPosition: ImageView = view!!.findViewById(R.id.playerPos3)
        val bitmap: Bitmap? = Bitmap.createBitmap(
            (1024 * multiplier).roundToInt(),
            (1024 * multiplier).roundToInt(),
            Bitmap.Config.ARGB_8888
        )
        var total = 0

        val rounds: JSONArray =
            jsonDetails.getJSONObject("data").getJSONArray("rounds")
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


                val finalVictimX: Int =
                    (((victimY.toInt() * xMult) + xScalar) * 1024 * multiplier).roundToInt()
                val finalVictimY: Int =
                    (((victimX.toInt() * yMult) + yScalar) * 1024 * multiplier).roundToInt()
                val finalKillerX: Int =
                    (((killerY * xMult) + xScalar) * 1024 * multiplier).roundToInt()
                val finalKillerY: Int =
                    (((killerX * yMult) + yScalar) * 1024 * multiplier).roundToInt()

                val killerAgentURL = mapofPlayerandAgent.getValue(killerName as String)
                val victimAgentURL = mapofPlayerandAgent.getValue(victimName as String)

                val paint = Paint()
                paint.style = Paint.Style.FILL
                paint.strokeWidth = 8F
                val radius: Float = 10.0F
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
                total += 1
            }
        }

        playerPosition.setImageBitmap(bitmap)
    }

    private fun startAnimation() {

    }

    private fun nextRoundAnimation() {
        //val spinner: Spinner = view!!.findViewById(R.id.roundSelectorOverview)
        val currentIndex = 0 //spinner.selectedItemPosition

        // show an alert dialog to the user that the next round will be loaded
        AlertDialog.Builder(requireContext())
            .setTitle("Play Next Round?")
            .setMessage("The next round will be loaded. Are you sure you want to continue?")
            .setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
                loadData(currentIndex + 1)
            }
            .show()
    }

    private fun TextView.setTextAnimation(
        text: String,
        duration: Long = 300,
        completion: (() -> Unit)? = null
    ) {
        fadOutAnimation(duration) {
            this.text = text
            fadInAnimation(duration) {
                completion?.let {
                    it()
                }
            }
        }
    }

    private fun View.fadOutAnimation(
        duration: Long = 300,
        visibility: Int = View.INVISIBLE,
        completion: (() -> Unit)? = null
    ) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                this.visibility = visibility
                completion?.let {
                    it()
                }
            }
    }

    private fun View.fadInAnimation(duration: Long = 300, completion: (() -> Unit)? = null) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction {
                completion?.let {
                    it()
                }
            }
    }

    fun View.getLocationOnScreen(): Point {
        val location = IntArray(2)
        this.getLocationOnScreen(location)
        return Point(location[0], location[1])
    }

}
