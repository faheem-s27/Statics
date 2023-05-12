package com.jawaadianinc.valorant_stats.valo.activities

import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.adapters.MMRAdapter
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.jawaadianinc.valorant_stats.valo.match_info.MatchHistoryActivity
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.GridLabelRenderer
import com.jjoe64.graphview.helper.DateAsXAxisLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URL
import kotlin.math.roundToInt

class MMRActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mmractivity)

        var riotName = intent.extras?.getString("RiotName")
        var riotID = intent.extras?.getString("RiotID")
        var region: String?

        // check if null
        if (riotName == null || riotID == null) {
            val playerName = PlayerDatabase(this).getPlayerName()
            if (playerName != null) {
                riotName = playerName.split("#")[0]
            }
            if (playerName != null) {
                riotID = playerName.split("#")[1]
            }
        }

        // get region
        val puuid = PlayerDatabase(this).getPUUID(riotName!!, riotID!!)
        region = PlayerDatabase(this).getRegion(puuid)

        // if region is null, then set region to eu
        if (region == null) {
            region = "eu"
        }

        val nameText: TextView = findViewById(R.id.MMRname)
        nameText.text = "$riotName#$riotID"

        val playerWideImage: ImageView = findViewById(R.id.playerCardWide)
        val playerLongImage: ImageView = findViewById(R.id.playerImageLong)
        val rankIcon: ImageView = findViewById(R.id.rankIcon)
        val rankProgress: ProgressBar = findViewById(R.id.rankProgress)
        val number: TextView = findViewById(R.id.number)
        val rankName: TextView = findViewById(R.id.rankName)

        // show loading dialog
        val progressDialog =
            ProgressDialogStatics().setProgressDialog(this, getString(R.string.s103))
        progressDialog.show()

        val tierURL = "https://valorant-api.com/v1/competitivetiers"
        val playerURL =
            "https://api.henrikdev.xyz/valorant/v1/account/${riotName}/$riotID" // modified
        val currentTier = "https://api.henrikdev.xyz/valorant/v1/mmr/$region/${riotName}/$riotID"
        val mmrHistory =
            "https://api.henrikdev.xyz/valorant/v1/mmr-history/$region/${riotName}/${riotID}"

        val graphView: GraphView = findViewById(R.id.rankGraph)
        val listOfMMRs = ArrayList<Int>()
        val listOfDates = ArrayList<Long>()

        doAsync {
            try {
                var data = Henrik(this@MMRActivity).henrikAPI(playerURL)
                data = data["data"] as JSONObject
                val cards = data["card"] as JSONObject
                val playerCard = cards["wide"].toString()
                val playerLongCard = cards["large"].toString()
                uiThread {
                    Picasso
                        .get()
                        .load(playerCard)
                        .transform(BlurTransformation(this@MMRActivity))
                        .fit()
                        .centerInside()
                        .into(playerWideImage)
                    Picasso
                        .get()
                        .load(playerLongCard)
                        .transform(BlurTransformation(this@MMRActivity))
                        .fit()
                        .centerCrop()
                        .into(playerLongImage)

                }

                val currentTierData = Henrik(this@MMRActivity).henrikAPI(currentTier)
                val dataofThis = currentTierData["data"] as JSONObject
                val currentTierNumber = dataofThis["currenttier"] as Int
                val progressNumber = dataofThis["ranking_in_tier"] as Int
                val patched = dataofThis["currenttierpatched"] as String

                uiThread {
                    rankProgress.progress = progressNumber
                    if (progressNumber <= 100) {
                        rankProgress.max = 100
                        number.text = "$progressNumber/100"
                    } else {
                        rankProgress.max = 1200
                        number.text = progressNumber.toString()
                    }
                    rankName.text = patched
                }
                val tiers = JSONObject(URL(tierURL).readText())
                val tierArray = tiers["data"] as JSONArray
                val tierData = tierArray[tierArray.length() - 1] as JSONObject
                val tiersagain = tierData["tiers"] as JSONArray
                for (j in 0 until tiersagain.length()) {
                    val actualTier = tiersagain[j] as JSONObject
                    val done = actualTier["tier"] as Int
                    if (done == currentTierNumber) {
                        val tierIcon = actualTier["largeIcon"] as String
                        runOnUiThread {
                            Picasso
                                .get()
                                .load(tierIcon)
                                .fit()
                                .centerInside()
                                .into(rankIcon)
                            rankIcon.resize(200, 200)
                            rankIcon.scaleType = ImageView.ScaleType.FIT_XY
                        }
                    }
                }

                val mmrArray = Henrik(this@MMRActivity).henrikAPI(mmrHistory)["data"] as JSONArray

                val dates = ArrayList<String>()
                val changes = ArrayList<String>()
                val numberMMR = ArrayList<String>()
                val rankname = ArrayList<String>()
                val rawDates = ArrayList<String>()
                val rankTriangles = ArrayList<String>()

                for (i in 0 until mmrArray.length()) {
                    val currentMMR = mmrArray[i] as JSONObject
                    val currentNumber = currentMMR["ranking_in_tier"] as Int
                    val date = currentMMR["date"] as String
                    val currentTiername = currentMMR["currenttierpatched"] as String
                    val change = currentMMR["mmr_change_to_last_game"] as Int
                    val rawDate = currentMMR["date_raw"] as Int

                    rankTriangles += if (change > 0) {
                        currentMMR.getJSONObject("images").getString("triangle_up")
                    } else {
                        currentMMR.getJSONObject("images").getString("triangle_down")
                    }

                    listOfMMRs += currentNumber
                    listOfDates += rawDate.toLong()

                    dates += date
                    changes += change.toString()
                    numberMMR += currentNumber.toString()
                    rankname += currentTiername
                    rawDates += rawDate.toString()
                }
                val scroll: ListView = findViewById(R.id.MMRList)

                uiThread {
                    val mmrAdapter =
                        MMRAdapter(
                            this@MMRActivity,
                            dates,
                            changes,
                            numberMMR,
                            rankname,
                            rankTriangles
                        )
                    scroll.adapter = mmrAdapter
                    progressDialog.dismiss()

                    // loop through the list of mmrs and add them to the graph
                    // reverse the list so that the graph is in order
                    listOfMMRs.reverse()
                    listOfDates.reverse()
                    val dataPoints = ArrayList<DataPoint>()
                    var series: LineGraphSeries<DataPoint> =
                        LineGraphSeries(dataPoints.toTypedArray())

                    graphView.viewport.isYAxisBoundsManual = true
                    graphView.viewport.setMaxY(100.0)
                    graphView.viewport.setMinY(0.0)

                    graphView.viewport.isScrollable = true
                    graphView.viewport.isScalable = true
                    graphView.titleTextSize = 50f
                    graphView.gridLabelRenderer.gridColor = getColor(R.color.white)
                    graphView.gridLabelRenderer.gridStyle = GridLabelRenderer.GridStyle.BOTH
                    graphView.gridLabelRenderer.labelVerticalWidth = 50
                    graphView.gridLabelRenderer.labelHorizontalHeight = 50

                    val datesSwtich = findViewById<Switch>(R.id.viewDatesMMRSwitch)

                    datesSwtich.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            // dates are enabled
                            graphView.removeAllSeries()
                            dataPoints.clear()
                            for (i in 0 until listOfMMRs.size) {
                                dataPoints.add(
                                    DataPoint(
                                        listOfDates[i].toDouble() * 1000,
                                        listOfMMRs[i].toDouble()
                                    )
                                )
                            }

                            series = LineGraphSeries(dataPoints.toTypedArray())
                            series.isDrawDataPoints = true
                            series.dataPointsRadius = 10f
                            series.thickness = 7
                            series.isDrawAsPath = true
                            series.color = getColor(R.color.white)

                            graphView.gridLabelRenderer.labelFormatter =
                                DateAsXAxisLabelFormatter(this@MMRActivity)
                            graphView.gridLabelRenderer.numHorizontalLabels = 3
                            graphView.animate()
                            graphView.addSeries(series)

                            graphView.viewport.isXAxisBoundsManual = true
                            graphView.viewport.setMinX(listOfDates[0].toDouble() * 1000)
                            graphView.viewport.setMaxX(listOfDates[listOfDates.size - 1].toDouble() * 1000)


                        } else {
                            // dates are disabled
                            graphView.removeAllSeries()
                            dataPoints.clear()
                            for (i in 0 until listOfMMRs.size) {
                                dataPoints.add(DataPoint(i.toDouble(), listOfMMRs[i].toDouble()))
                            }

                            series = LineGraphSeries(dataPoints.toTypedArray())
                            series.isDrawDataPoints = true
                            series.dataPointsRadius = 10f
                            series.thickness = 7
                            series.isDrawAsPath = true
                            series.color = getColor(R.color.white)

                            graphView.viewport.isXAxisBoundsManual = true
                            graphView.viewport.setMinX(0.0)
                            graphView.viewport.setMaxX(listOfMMRs.size.toDouble() - 1)

                            graphView.gridLabelRenderer.labelFormatter =
                                DefaultLabelFormatter()
                            graphView.gridLabelRenderer.numHorizontalLabels = listOfMMRs.size
                            graphView.animate()
                            graphView.addSeries(series)

                            graphView.viewport.isXAxisBoundsManual = false
                        }
                    }

                    graphView.removeAllSeries()
                    dataPoints.clear()
                    for (i in 0 until listOfMMRs.size) {
                        dataPoints.add(DataPoint(i.toDouble(), listOfMMRs[i].toDouble()))
                    }

                    series = LineGraphSeries(dataPoints.toTypedArray())
                    series.isDrawDataPoints = true
                    series.dataPointsRadius = 10f
                    series.thickness = 7
                    series.isDrawAsPath = true
                    series.color = getColor(R.color.white)

                    graphView.viewport.isXAxisBoundsManual = true
                    graphView.viewport.setMinX(0.0)
                    graphView.viewport.setMaxX(listOfMMRs.size.toDouble() - 1)

                    graphView.gridLabelRenderer.labelFormatter =
                        DefaultLabelFormatter()
                    graphView.gridLabelRenderer.numHorizontalLabels = listOfMMRs.size
                    graphView.animate()
                    graphView.addSeries(series)

                    graphView.viewport.isXAxisBoundsManual = false

                    scroll.setOnItemClickListener { _, _, position, _ ->
                        val rawDate = rawDates[position]
                        trytoFindMatch(rawDate.toInt())
                    }
                }

            } catch (e: JSONException) {
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@MMRActivity).setTitle("Error getting rank!")
                        .setMessage("This user is either unranked or hasn't played competitive in a long time.\n\nOr a random error occurred and you should try again.")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            finish()
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()

                    // send firebase crash report
                    FirebaseAnalytics.getInstance(this@MMRActivity)
                        .logEvent("errorRank", Bundle().apply {
                            putString("error", e.toString())
                        })
                }
            } catch (e: Exception) {
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@MMRActivity).setTitle("Error!")
                        .setMessage("There was an issue getting the ranking :/ Error details have been sent to the developer and will be fixed soon.")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            finish()
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                    FirebaseAnalytics.getInstance(this@MMRActivity)
                        .logEvent("errorRank", Bundle().apply {
                            putString("error", e.toString())
                        })
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        FirebaseApp.initializeApp(/*context=*/this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )
    }

    private fun trytoFindMatch(gameStarting: Int) {
        val progressDialog =
            ProgressDialogStatics().setProgressDialog(this, getString(R.string.s105))
        progressDialog.show()

        try {
            val riotName = intent.extras!!.getString("RiotName")
            val riotID = intent.extras!!.getString("RiotID")
            val puuid = PlayerDatabase(this).getPUUID(riotName!!, riotID!!)
            val region = PlayerDatabase(this).getRegion(puuid!!)
            val key = intent.extras!!.getString("key")
            val matchHistoryURL =
                "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
            doAsync {
                val jsonMatches = JSONObject(URL(matchHistoryURL).readText())
                val dataforMatch = jsonMatches["history"] as JSONArray
                var matchID = ""

                for (i in 0 until dataforMatch.length()) {
                    val specificMatch = dataforMatch[i] as JSONObject
                    val startGameID = specificMatch["gameStartTimeMillis"] as Long
                    val inSeconds: Float = (startGameID / 1000).toFloat()

                    val topSecond = inSeconds.roundToInt() + 300
                    val bottomSecond = inSeconds.roundToInt() - 300

                    if (gameStarting in bottomSecond..topSecond) {
                        matchID = specificMatch["matchId"] as String
                        break
                    }
                }
                uiThread {
                    progressDialog.dismiss()
                    if (matchID != "") {
                        val matchintent =
                            Intent(this@MMRActivity, MatchHistoryActivity::class.java)
                        matchintent.putExtra("RiotName", riotName)
                        matchintent.putExtra("RiotID", riotID)
                        matchintent.putExtra("MatchNumber", 0)
                        matchintent.putExtra("MatchID", matchID)
                        startActivity(matchintent)
                    } else {
                        Toast.makeText(
                            this@MMRActivity,
                            "Match was not found.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        } catch (e: NullPointerException) {
            Toast.makeText(this, "Cannot view matches from other users!", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
            progressDialog.dismiss()
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
