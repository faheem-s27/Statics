package com.jawaadianinc.valorant_stats.valorant

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
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
        doAsync {
            try {
                val jsonAgents: JSONArray =
                    JSONObject(URL("https://valorant-api.com/v1/agents").readText()).getJSONArray("data")

                val jsonDetails = MatchHistoryActivity.matchJSON
                val matchData = jsonDetails?.get("data") as JSONObject
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
                val playerCharacters = ArrayList<String>()

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
                    playerCharacters += currentPlayer["character"] as String
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
                    playerCharacters += currentPlayer["character"] as String
                }

                val playerList: ListView = view.findViewById(R.id.playerList)


                uiThread {
                    val mediaPlayer = MediaPlayer()
                    mediaPlayer.start()
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
                        val popupView: View = inflater.inflate(R.layout.showplayerinfoval, null)
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

                        for (i in 0 until jsonAgents.length()) {
                            val currentAgent = jsonAgents[i] as JSONObject
                            if (currentAgent.getString("displayName") == playerCharacters[position]) {
                                val details = currentAgent.getJSONObject("voiceLine")
                                    .getJSONArray("mediaList") as JSONArray
                                val voiceLineURL = details[0] as JSONObject
                                val url = voiceLineURL.getString("wave")
                                doAsync {
                                    mediaPlayer.apply {
                                        setAudioAttributes(
                                            AudioAttributes.Builder()
                                                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                                                .setUsage(AudioAttributes.USAGE_MEDIA)
                                                .build()
                                        )
                                        setDataSource(url)
                                        prepare()
                                        start()
                                    }
                                }
                            }
                        }

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
                            mediaPlayer.reset()
                            popupWindow.dismiss()
                            dim_layout.visibility = View.INVISIBLE
                        }

                        val copyButton: Button = popupView.findViewById(R.id.copyPlayerName)
                        copyButton.setOnClickListener {
                            copyText(playerNames[position])
                            Toast.makeText(requireActivity(), "Copied Name!", Toast.LENGTH_SHORT)
                                .show()
                        }

                        val rankButton: Button = popupView.findViewById(R.id.playerRankButon)
                        rankButton.setOnClickListener {
                            val name = playerNames[position].split("#")
                            val intent = Intent(requireActivity(), MMRActivity::class.java)
                            intent.putExtra("RiotName", name[0])
                            intent.putExtra("RiotID", name[1])
                            startActivity(intent)
                        }

                        val agentInfo: Button = popupView.findViewById(R.id.agentInfo)
                        agentInfo.setOnClickListener {
                            val inflater =
                                requireActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                            val popupView: View = inflater.inflate(R.layout.showupdates, null)
                            val width = LinearLayout.LayoutParams.MATCH_PARENT
                            val height = LinearLayout.LayoutParams.MATCH_PARENT
                            val focusable = true
                            val popupWindow = PopupWindow(popupView, width, height, focusable)
                            popupWindow.showAtLocation(
                                View(requireActivity()),
                                Gravity.CENTER,
                                0,
                                0
                            )

                            val dismissButton = popupView.findViewById(R.id.dismiss) as Button
                            dismissButton.setOnClickListener {
                                popupWindow.dismiss()
                            }
                            val webpage = popupView.findViewById(R.id.updatePage) as WebView
                            webpage.settings.javaScriptEnabled = true
                            webpage.loadUrl("https://playvalorant.com/en-us/agents/" + playerCharacters[position].lowercase() + "/")
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
