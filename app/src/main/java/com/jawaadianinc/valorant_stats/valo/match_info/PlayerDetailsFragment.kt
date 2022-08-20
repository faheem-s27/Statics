package com.jawaadianinc.valorant_stats.valo.match_info

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.MMRActivity
import com.jawaadianinc.valorant_stats.valo.adapters.PlayerAdapter
import com.squareup.picasso.Picasso
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
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

    var jsonRanksArray = JSONArray()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        doAsync {
            try {
                jsonRanksArray =
                    JSONObject(URL("https://valorant-api.com/v1/competitivetiers").readText()).getJSONArray(
                        "data"
                    )
                val rankIndex = jsonRanksArray.length() - 1

                val jsonDetails = MatchHistoryActivity.matchJSON
                val matchData = jsonDetails.get("data") as JSONObject
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
                val playerTier = ArrayList<String>()

                var number = 0


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

                    var RankURL = ""
                    // Get the tier name from the API
                    val tier = currentPlayer.getInt("currenttier")
                    val tiers = jsonRanksArray.getJSONObject(rankIndex).getJSONArray("tiers")
                    for (j in 1 until tiers.length()) {
                        val currentTier = tiers[j] as JSONObject
                        if (currentTier.getInt("tier") == tier) {
                            RankURL = currentTier.getString("largeIcon")
                            break
                        }
                    }

                    // if the rankURL is empty, then try to get the tier name from the API using the getRank()
                    if (RankURL == "") {
                        RankURL = getRankURL(
                            currentPlayer["name"] as String,
                            currentPlayer["tag"] as String
                        )
                    }

                    number += 1
                    uiThread {
                        view.findViewById<TextView>(R.id.textView20).text =
                            "Processed $number players"
                    }
                    playerTier += RankURL
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

                    var RankURL = ""
                    // Get the tier name from the API
                    val tier = currentPlayer.getInt("currenttier")

                    val tiers = jsonRanksArray.getJSONObject(rankIndex).getJSONArray("tiers")
                    for (j in 1 until tiers.length()) {
                        val currentTier = tiers[j] as JSONObject
                        if (currentTier.getInt("tier") == tier) {
                            RankURL = currentTier.getString("largeIcon")
                            break
                        }
                    }

                    // if the rankURL is empty, then try to get the tier name from the API using the getRank()
                    if (RankURL == "") {
                        RankURL = getRankURL(
                            currentPlayer["name"] as String,
                            currentPlayer["tag"] as String
                        )
                    }

                    number += 1
                    uiThread {
                        view.findViewById<TextView>(R.id.textView20).text =
                            "Processed $number players"
                    }

                    playerTier += RankURL
                }

                val playerList: ListView = view.findViewById(R.id.playerList)


                uiThread {

                    // find textview 20 and progressbar 5 and set the visibility to gone
                    view.findViewById<TextView>(R.id.textView20).visibility = View.GONE
                    view.findViewById<ProgressBar>(R.id.progressBar5).visibility = View.GONE

                    val players = PlayerAdapter(
                        requireActivity(),
                        playerAgentURL,
                        playerTeam,
                        playerNames,
                        playerKills,
                        playerDeaths,
                        playerAssists,
                        playerTier
                    )
                    playerList.adapter = players
                    playerList.setOnItemClickListener { _, _, position, _ ->
                        val inflater =
                            view.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val popupView: View = inflater.inflate(R.layout.showplayerinfoval, null)
                        val width = LinearLayout.LayoutParams.MATCH_PARENT
                        val height = LinearLayout.LayoutParams.MATCH_PARENT
                        val focusable = true
                        val popupWindow = PopupWindow(popupView, width, height, focusable)
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

                        val rankButton: Button = popupView.findViewById(R.id.playerRankButon)
                        rankButton.setOnClickListener {
                            val name = playerNames[position].split("#")
                            val intent = Intent(requireActivity(), MMRActivity::class.java)
                            intent.putExtra("RiotName", name[0])
                            intent.putExtra("RiotID", name[1])
                            startActivity(intent)
                        }

                    }
                }
            } catch (e: Exception) {
                uiThread {
                    AlertDialog.Builder(requireActivity()).setTitle("Error!")
                        .setMessage("Something unexpected occurred when loading players data! \nError: $e")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
        }
    }


    private fun getRankURL(name: String, tag: String): String {
        try {
            var rankURL = ""
            // attempt to get all of the players rank in unrated or other modes
            val tierRanking =
                HenrikAPI("https://api.henrikdev.xyz/valorant/v1/mmr/eu/$name/$tag").getJSONObject("data")
                    .getInt("currenttier")
            val rankIndex = jsonRanksArray.length() - 1
            val tiers = jsonRanksArray.getJSONObject(rankIndex).getJSONArray("tiers")
            for (j in 0 until tiers.length()) {
                val currentTier = tiers[j] as JSONObject
                if (currentTier.getInt("tier") == tierRanking) {
                    rankURL = currentTier.getString("largeIcon")
                    break
                }
            }
            Log.d("Henrik", "RankURL for $name#$tag: $rankURL")
            return rankURL
        } catch (e: Exception) {
            Log.d("Henrik", "Error getting rankURL for $name#$tag")
            return ""
        }

    }

    private fun copyText(text: String) {
        val myClipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("Label", text)
        myClipboard.setPrimaryClip(myClip)
    }

    private fun HenrikAPI(playerURL: String): JSONObject {
        return executeRequest(playerURL)
    }

    private fun executeRequest(playerURL: String): JSONObject {
        val client = OkHttpClient()
        val urlBuilder: HttpUrl.Builder =
            playerURL.toHttpUrlOrNull()!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "HDEV-67e86af9-8bf9-4f6d-b628-f4521b20d772")
            .build()
        return JSONObject(client.newCall(request).execute().body.string())
    }

}
