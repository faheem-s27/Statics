package com.jawaadianinc.valorant_stats

import android.app.ProgressDialog
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
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


class MatchDetailsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_match_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Fetching Match data")
        progressDialog.setMessage("Please wait a moment")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) // There are 3 styles, You'll figure it out :)
        progressDialog.setCancelable(false)
        progressDialog.show()

        val Name = requireActivity().intent.extras!!.getString("RiotName")
        val ID = requireActivity().intent.extras!!.getString("RiotID")
        val MatchNumber = requireActivity().intent.extras!!.getInt("MatchNumber")
        val IDofMatch = requireActivity().intent.extras!!.getString("MatchID")


        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID?size=10"

        doAsync {
            try {
                val matchID: String = if (IDofMatch == "none") {
                    val matchhistoryURL = URL(allmatches).readText()
                    val jsonMatches = JSONObject(matchhistoryURL)
                    val data = jsonMatches["data"] as JSONArray
                    val easier = data.getJSONObject(MatchNumber).getJSONObject("metadata")
                    easier.getString("matchid")
                } else {
                    IDofMatch!!
                }
                val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"
                val getMapImage = "https://api.tracker.gg/api/v2/valorant/rap-matches/$matchID"

                val matchdetailsURL = URL(matchURl).readText()
                val jsonDetails = JSONObject(matchdetailsURL)
                val matchData = jsonDetails["data"] as JSONObject

                var actualtMapUlr = ""
                try {
                    val matchMapURl = URL(getMapImage).readText()
                    val jsonMapImage = JSONObject(matchMapURl)
                    val mapURL = jsonMapImage["data"] as JSONObject
                    actualtMapUlr = mapURL.getJSONObject("metadata").getString("mapImageUrl")
                }
                catch (e:Exception){}


                val arrayList = ArrayList<String>()
                val listviewComp : ListView = view.findViewById(R.id.listViewMatchDetails)
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

                        return item
                    }
                }

                uiThread {
                    listviewComp.adapter = mAdapter
                    val metadata = matchData.getJSONObject("metadata")
                    val map = metadata.getString("map")
                    val game_started = metadata.getString("game_start_patched")
                    val roundsPlayed = metadata.getString("rounds_played")
                    val mode = metadata.getString("mode")
                    val timePlayed = metadata.getInt("game_length")
                    val server = metadata.getString("cluster")
                    val inMinutes = timePlayed / 60000

                    val teams = matchData.getJSONObject("teams")
                    var didredWin = false

                    try {
                        didredWin = teams.getJSONObject("red").getBoolean("has_won")
                    } catch (e: Exception) {
                        Toast.makeText(
                            activity?.applicationContext!!,
                            "ERROR: $e",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    mAdapter.add("Map: $map")
                    mAdapter.add("Mode: $mode")
                    mAdapter.add("Server: $server")
                    mAdapter.add("Started: $game_started")
                    mAdapter.add("Duration: $inMinutes minutes")
                    mAdapter.add("Rounds Played: $roundsPlayed")

                    if (didredWin) {
                        mAdapter.add("Red won!")
                        val score = teams.getJSONObject("red").getString("rounds_won")
                        val lost = teams.getJSONObject("red").getString("rounds_lost")
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
                    val mapImage : ImageView = view.findViewById(R.id.mapURL)
                    if (actualtMapUlr !== "") {
                        Picasso.get().load(actualtMapUlr).into(mapImage)
                    }

                    progressDialog.dismiss()

                }


            }
            catch (e:Exception){
                uiThread {
                    progressDialog.dismiss()
                    AlertDialog.Builder(requireActivity()).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }

    }
}