package com.jawaadianinc.valorant_stats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


class PlayerDetailsFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player_details, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
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
                Log.d("Match", matchURl)
                val matchdetailsURL = URL(matchURl).readText()
                val jsonDetails = JSONObject(matchdetailsURL)
                val matchData = jsonDetails["data"] as JSONObject
                val players = matchData.getJSONObject("players")
                val bloo = players.getJSONArray("blue")
                val red = players.getJSONArray("red")
                val playerNames = ArrayList<String>()
                val playerTeam = ArrayList<String>()
                val playerLevels = ArrayList<String>()
                val playerRanks = ArrayList<String>()
                val playerAgentURL = ArrayList<String>()
                val playerScore = ArrayList<String>()
                val playerKills = ArrayList<String>()
                val playerDeaths = ArrayList<String>()
                val playerAssists = ArrayList<String>()

                for (i in 0 until red.length()) {
                    val currentPlayer = red[i] as JSONObject
                    val stats = currentPlayer["stats"] as JSONObject
                    val assets = currentPlayer["assets"] as JSONObject
                    val agent = assets["agent"] as JSONObject
                    playerNames += "${currentPlayer["name"]}#${currentPlayer["tag"]}"
                    playerTeam += currentPlayer["team"] as String
                    playerLevels += currentPlayer.getString("level")
                    playerKills += stats.getString("kills")
                    playerDeaths += stats.getString("deaths")
                    playerAssists += stats.getString("assists")
                    playerScore += stats.getString("score")
                    playerAgentURL += agent["small"] as String
                    playerRanks += currentPlayer["currenttier_patched"] as String
                }
                for (i in 0 until bloo.length()) {
                    val currentPlayer = bloo[i] as JSONObject
                    val stats = currentPlayer["stats"] as JSONObject
                    val assets = currentPlayer["assets"] as JSONObject
                    val agent = assets["agent"] as JSONObject
                    playerNames += "${currentPlayer["name"]}#${currentPlayer["tag"]}"
                    playerTeam += currentPlayer["team"] as String
                    playerLevels += currentPlayer.getString("level")
                    playerKills += stats.getString("kills")
                    playerDeaths += stats.getString("deaths")
                    playerAssists += stats.getString("assists")
                    playerScore += stats.getString("score")
                    playerAgentURL += agent["small"] as String
                    playerRanks += currentPlayer["currenttier_patched"] as String
                }

                val playerList: ListView = view.findViewById(R.id.playerList)

                uiThread {
                    val players = PlayerAdapter(
                        requireActivity(),
                        playerAgentURL,
                        playerTeam,
                        playerNames,
                        playerScore,
                        playerKills,
                        playerDeaths,
                        playerAssists
                    )
                    playerList.adapter = players
                    playerList.setOnItemClickListener { _, _, position, _ ->
                        val inflater =
                            view.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val popupView: View = inflater.inflate(R.layout.showredplayer, null)
                        val width = LinearLayout.LayoutParams.MATCH_PARENT
                        val height = LinearLayout.LayoutParams.MATCH_PARENT
                        val focusable = true
                        var popupWindow = PopupWindow(popupView, width, height, focusable)
                        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
                        val dim_layout = view.findViewById(R.id.dim_layout) as LinearLayout
                        dim_layout.visibility = View.VISIBLE

                        val playernameTitle = popupView.findViewById<TextView>(R.id.playerName)
                        playernameTitle.text = playerNames[position]
                        val playerImage: ImageView = popupView.findViewById(R.id.playerImage)
                        Picasso.get().load(playerAgentURL[position]).into(playerImage)
                        val playerstats = popupView.findViewById<TextView>(R.id.playerstatsText)
                        val tier = playerRanks[position]
                        val level = playerLevels[position]
                        val score = playerScore[position]
                        val kills = playerKills[position]
                        val deaths = playerDeaths[position]
                        val assists = playerAssists[position]

                        if (tier == "Unrated") {
                            playerstats.text = "Level: $level" +
                                    "\nScore: $score" +
                                    "\nKills: $kills" +
                                    "\nDeaths: $deaths" +
                                    "\nAssists: $assists"
                        } else {
                            playerstats.text = "Rank: $tier\nLevel: $level" +
                                    "\nScore: $score" +
                                    "\nKills: $kills" +
                                    "\nDeaths: $deaths" +
                                    "\nAssists: $assists"
                        }
                        val dismissbutton: Button = popupView.findViewById(R.id.dismiss)
                        dismissbutton.setOnClickListener {
                            popupWindow.dismiss()
                            dim_layout.visibility = View.INVISIBLE
                        }

                        val copyButton: Button = popupView.findViewById(R.id.copyPlayerName)
                        copyButton.setOnClickListener {
                            copyText(playerNames[position])
                            Toast.makeText(requireActivity(), "Copied Name!", Toast.LENGTH_SHORT)
                                .show()
                        }

                    }
                }
            }
            catch (e: Exception){
                uiThread {
                    AlertDialog.Builder(requireActivity()).setTitle("Error!")
                        .setMessage("Error Message: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }
    }

    private fun copyText(text: String) {
        val myClipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("Label", text)
        myClipboard.setPrimaryClip(myClip)
    }
}
