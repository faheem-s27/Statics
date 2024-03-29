package com.jawaadianinc.valorant_stats.valo.match_info

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
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
import java.time.Duration
import java.time.Instant
import java.util.Date


class MatchDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val progressDialog =
            ProgressDialogStatics().setProgressDialog(requireActivity(), "Loading Match Details")
        progressDialog.show()


        doAsync {
            try {
                val jsonOfMap = JSONObject(URL("https://valorant-api.com/v1/maps").readText())
                val mapData = jsonOfMap["data"] as JSONArray
                val jsonDetails = MatchHistoryActivity.matchJSON
                val matchData = jsonDetails.get("data") as JSONObject
                val metadata = matchData.getJSONObject("metadata")
                val map = metadata.getString("map")

                var actualtMapUlr = ""
                for (i in 0 until mapData.length()) {
                    val mapNamefromJSON = mapData[i] as JSONObject
                    val nameofMpa = mapNamefromJSON["displayName"]
                    if (nameofMpa == map) {
                        actualtMapUlr = mapNamefromJSON["splash"].toString()
                    }
                }

                val teams = matchData.getJSONObject("teams")
                val colour = if (teams.getJSONObject("red").getBoolean("has_won")) {
                    "#f94555"
                } else {
                    "#18e4b7"
                }

                val arrayList = ArrayList<String>()
                val listviewComp: ListView = view.findViewById(R.id.listViewMatchDetails)
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

                        // if the color is red, make the background red to black gradient
                        if (colour == "#f94555") {
                            item.background = context.getDrawable(R.drawable.red_to_black)
                        } else {
                            item.background = context.getDrawable(R.drawable.blue_to_black)
                        }

                        return item
                    }
                }

                uiThread {
                    listviewComp.adapter = mAdapter
                    val matchMetadata = matchData.getJSONObject("metadata")
                    val gameStarted = matchMetadata.getString("game_start_patched")
                    val roundsPlayed = matchMetadata.getString("rounds_played")
                    val mode = matchMetadata.getString("mode")
                    val timePlayed = matchMetadata.getInt("game_length")
                    val server = matchMetadata.getString("cluster")
                    val inMinutes = timePlayed / 60000
                    var didredWin = false

                    try {
                        didredWin = teams.getJSONObject("red").getBoolean("has_won")
                    } catch (e: Exception) {
                        Toast.makeText(
                            activity?.applicationContext!!,
                            "Cannot do stats for this game mode (yet)!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    val unixTimeStart = matchMetadata.getInt("game_start")
                    val date = Date(unixTimeStart * 1000L)
                    val d: Duration =
                        Duration.between(
                            date.toInstant(),
                            Instant.now()
                        )
                    if (didredWin) {
                        mAdapter.add(getString(R.string.s219))
                        val score = teams.getJSONObject("red").getString("rounds_won")
                        val lost: String = teams.getJSONObject("red").getString("rounds_lost")
                        mAdapter.add("${getString(R.string.s220)}: $score : $lost")
                    } else {
                        mAdapter.add(getString(R.string.s218))
                        try {
                            val score = teams.getJSONObject("blue").getString("rounds_won")
                            val lost = teams.getJSONObject("blue").getString("rounds_lost")
                            mAdapter.add("${getString(R.string.s220)}: $score : $lost")
                        } catch (e: Exception) {
                        }
                    }

                    try {
                        val timeinDays = d.toDays()
                        val timeInHours = d.toHours()
                        when {
                            timeinDays > 0 -> {
                                mAdapter.add("${getString(R.string.s224)} $timeinDays ${getString(R.string.s223)}")
                            }
                            timeInHours > 0 -> {
                                mAdapter.add("${getString(R.string.s224)} $timeInHours ${getString(R.string.s221)}")
                            }

                            else -> {
                                mAdapter.add("${getString(R.string.s224)} ${d.toMinutes()} ${getString(R.string.s222)}")
                            }
                        }
                    } catch (e: Exception) {
                    }

                    mAdapter.add("${getString(R.string.s206)}: $roundsPlayed")
                    mAdapter.add("${getString(R.string.s192)}: $map")
                    mAdapter.add("${getString(R.string.s201)}: $mode")
                    mAdapter.add("${getString(R.string.s202)}: $server")
                    mAdapter.add("${getString(R.string.s203)}: $gameStarted")
                    mAdapter.add("${getString(R.string.s204)}: $inMinutes minutes")


                    val mapImage: ImageView = view.findViewById(R.id.mapURL)
                    if (actualtMapUlr !== "") {
                        Picasso.get().load(actualtMapUlr).into(mapImage)
                    }

                    progressDialog.dismiss()
                    val progress: ProgressBar = view.findViewById(R.id.progress)
                    progress.visibility = View.INVISIBLE
                }


            } catch (e: Exception) {
                uiThread {
                    progressDialog.dismiss()
                    val progress: ProgressBar = view.findViewById(R.id.progress)
                    progress.visibility = View.INVISIBLE
                    AlertDialog.Builder(requireActivity()).setTitle("Error!")
                        .setMessage(getString(R.string.s205))
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }
    }

}
