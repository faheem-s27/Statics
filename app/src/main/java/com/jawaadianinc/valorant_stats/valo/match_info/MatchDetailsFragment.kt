package com.jawaadianinc.valorant_stats.valo.match_info

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
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
import java.util.*


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
            ProgressDialogStatics().setProgressDialog(activity!!, "Loading Match Details")
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
                        mAdapter.add("Red won!")
                        val score = teams.getJSONObject("red").getString("rounds_won")
                        val lost: String = teams.getJSONObject("red").getString("rounds_lost")
                        mAdapter.add("Score: $score : $lost")
                    } else {
                        mAdapter.add("Blue won!")
                        try {
                            val score = teams.getJSONObject("blue").getString("rounds_won")
                            val lost = teams.getJSONObject("blue").getString("rounds_lost")
                            mAdapter.add("Score: $score : $lost")
                        } catch (e: Exception) {
                        }
                    }

                    try {
                        val timeinDays = d.toDays()
                        val timeInHours = d.toHours()
                        when {
                            timeinDays > 0 -> {
                                mAdapter.add("Played $timeinDays days ago")
                            }
                            timeInHours > 0 -> {
                                mAdapter.add("Played $timeInHours hours ago")
                            }
                            else -> {
                                mAdapter.add("Played ${d.toMinutes()} minutes ago")
                            }
                        }
                    } catch (e: Exception) {
                    }

                    mAdapter.add("Rounds Played: $roundsPlayed")
                    mAdapter.add("Map: $map")
                    mAdapter.add("Mode: $mode")
                    mAdapter.add("Server: $server")
                    mAdapter.add("Started: $gameStarted")
                    mAdapter.add("Duration: $inMinutes minutes")


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
                        .setMessage("There was an an error loading the match due to an unsupported game mode. Please try a different match.")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }
    }

}
