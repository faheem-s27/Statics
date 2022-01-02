package com.jawaadianinc.valorant_stats

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.net.URL

class MMRActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mmractivity)

        val RiotName = intent.extras!!.getString("RiotName")
        val RiotID = intent.extras!!.getString("RiotID")
        val nameText: TextView = findViewById(R.id.MMRname)
        nameText.text = "$RiotName#$RiotID"

        val playerWideImage: ImageView = findViewById(R.id.playerCardWide)
        val playerLongImage: ImageView = findViewById(R.id.playerImageLong)
        val rankIcon: ImageView = findViewById(R.id.rankIcon)
        val rankProgress: ProgressBar = findViewById(R.id.rankProgress)
        val number: TextView = findViewById(R.id.number)
        val rankName: TextView = findViewById(R.id.rankName)

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Gathering rank info!")
        progressDialog.setMessage("Compiling information.")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.show()

        val tierURL = "https://valorant-api.com/v1/competitivetiers"
        val PlayerURL =
            "https://api.henrikdev.xyz/valorant/v1/account/${RiotName}/$RiotID?force=true"
        val currentTier = "https://api.henrikdev.xyz/valorant/v1/mmr/eu/${RiotName}/$RiotID"
        val MMRHistory =
            "https://api.henrikdev.xyz/valorant/v1/mmr-history/eu/${RiotName}/${RiotID}"


        doAsync {
            try {
                val text = URL(PlayerURL).readText()
                var data = JSONObject(text)
                data = data["data"] as JSONObject
                val cards = data["card"] as JSONObject
                val playerCard = cards["wide"].toString()
                val playerLongCard = cards["large"].toString()
                uiThread {
                    Picasso
                        .get()
                        .load(playerCard)
                        .fit()
                        .centerInside()
                        .into(playerWideImage)
                    Picasso
                        .get()
                        .load(playerLongCard)
                        .fit()
                        .centerCrop()
                        .into(playerLongImage)
                }

                val currentTierData = JSONObject(URL(currentTier).readText())
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
                val tierData = tierArray[3] as JSONObject
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
                            rankIcon.scaleType = ImageView.ScaleType.FIT_XY;
                        }
                    }
                }

                val MMRHistoryJSON = JSONObject(URL(MMRHistory).readText())
                val MMRArray = MMRHistoryJSON["data"] as JSONArray

                val dates = ArrayList<String>()
                val changes = ArrayList<String>()
                val numberMMR = ArrayList<String>()
                val rankname = ArrayList<String>()
                val rawDates = ArrayList<String>()

                for (i in 0 until MMRArray.length()) {
                    val currentMMR = MMRArray[i] as JSONObject
                    val number = currentMMR["ranking_in_tier"] as Int
                    val date = currentMMR["date"] as String
                    val currentTiername = currentMMR["currenttierpatched"] as String
                    val change = currentMMR["mmr_change_to_last_game"] as Int
                    val RAW = currentMMR["date_raw"] as Int
                    dates += date
                    changes += change.toString()
                    numberMMR += number.toString()
                    rankname += currentTiername
                    rawDates += RAW.toString()
                }
                val scroll: ListView = findViewById(R.id.MMRList)

                uiThread {
                    val MMRList =
                        MMRAdapter(this@MMRActivity, dates, changes, numberMMR, rankname)
                    scroll.adapter = MMRList

                    val textView = TextView(this@MMRActivity)
                    textView.setTypeface(Typeface.DEFAULT_BOLD)
                    textView.text = "Previous rank changes"
                    textView.gravity = Gravity.CENTER
                    textView.textSize = 30f
                    scroll.addHeaderView(textView)
                    progressDialog.dismiss()

                    scroll.setOnItemClickListener { _, _, position, _ ->
                        val rawDate = rawDates[position - 1]
                        trytoFindMatch(rawDate.toInt())
                    }
                }

            } catch (e: JSONException) {
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@MMRActivity).setTitle("Unranked!")
                        .setMessage("This user is either unranked or hasn't played competitive in a long time.")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            startActivity(Intent(this@MMRActivity, FindAccount::class.java))
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            } catch (e: Exception) {
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@MMRActivity).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }

        }
    }

    private fun trytoFindMatch(gameStarting: Int) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Tracing back match!")
        progressDialog.setMessage("Attempting to find match (Not always successful!)")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.show()

        val RiotName = intent.extras!!.getString("RiotName")
        val RiotID = intent.extras!!.getString("RiotID")
        val matchHistoryURL =
            "https://api.henrikdev.xyz/valorant/v3/matches/eu/${RiotName}/${RiotID}?size=10"
        doAsync {
            val jsonMatches = JSONObject(URL(matchHistoryURL).readText())
            val dataforMatch = jsonMatches["data"] as JSONArray
            var matchID: String = ""

            for (i in 0 until dataforMatch.length()) {
                val specificMatch = dataforMatch[i] as JSONObject
                val match = specificMatch["metadata"] as JSONObject
                val startGameID = match["game_start"] as Int
                if (startGameID == gameStarting) {
                    matchID = match["matchid"] as String
                }
            }
            uiThread {
                progressDialog.dismiss()
                if (matchID != "") {
                    val matchintent = Intent(this@MMRActivity, MatchHistoryActivity::class.java)
                    matchintent.putExtra("RiotName", RiotName)
                    matchintent.putExtra("RiotID", RiotID)
                    matchintent.putExtra("MatchNumber", 0)
                    matchintent.putExtra("MatchID", matchID)
                    startActivity(matchintent)
                } else {
                    Toast.makeText(
                        this@MMRActivity,
                        "Couldn't find match :(",
                        Toast.LENGTH_SHORT
                    ).show()
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
