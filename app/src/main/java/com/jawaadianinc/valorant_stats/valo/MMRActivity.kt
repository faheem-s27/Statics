package com.jawaadianinc.valorant_stats.valo

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.jawaadianinc.valorant_stats.R
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

        val RiotName = intent.extras!!.getString("RiotName")
        val RiotID = intent.extras!!.getString("RiotID")
        val nameText: TextView = findViewById(R.id.MMRname)
        nameText.text = "$RiotName#$RiotID\nPrevious ranks"

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
            "https://api.henrikdev.xyz/valorant/v1/account/${RiotName}/$RiotID"
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
                        .transform(BlurTransformation(this@MMRActivity))
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
                            rankIcon.scaleType = ImageView.ScaleType.FIT_XY
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
                    progressDialog.dismiss()

                    scroll.setOnItemClickListener { _, _, position, _ ->
                        val rawDate = rawDates[position]
                        trytoFindMatch(rawDate.toInt())
                    }
                }

            } catch (e: JSONException) {
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@MMRActivity).setTitle("Unranked!")
                        .setMessage("This user is either unranked or hasn't played competitive in a long time.")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            startActivity(Intent(this@MMRActivity, ValorantMainMenu::class.java))
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            } catch (e: Exception) {
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(this@MMRActivity).setTitle("Error!")
                        .setMessage("There was an issue getting the ranking :/ Error details have been sent to the developer")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            startActivity(Intent(this@MMRActivity, ValorantMainMenu::class.java))
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
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
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Finding match!")
        progressDialog.setMessage("Please wait...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.show()

        try {
            val RiotName = intent.extras!!.getString("RiotName")
            val RiotID = intent.extras!!.getString("RiotID")
            val puuid = PlayerDatabase(this).getPUUID(RiotName!!, RiotID!!)
            val region = PlayerDatabase(this).getRegion(puuid!!)
            val key = intent.extras!!.getString("key")
            val matchHistoryURL =
                "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
            doAsync {
                val jsonMatches = JSONObject(URL(matchHistoryURL).readText())
                val dataforMatch = jsonMatches["history"] as JSONArray
                var matchID: String = ""

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
