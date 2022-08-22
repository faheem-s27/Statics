package com.jawaadianinc.valorant_stats.valo.match_info

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.MMRActivity
import com.jawaadianinc.valorant_stats.valo.adapters.PlayersAdapter
import com.jawaadianinc.valorant_stats.valo.classes.Agent
import com.jawaadianinc.valorant_stats.valo.classes.Player
import com.jawaadianinc.valorant_stats.valo.classes.Rank
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

    private var jsonRanksArray = JSONArray()
    private val players = ArrayList<Player>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rg = view.findViewById(R.id.radioGroup) as RadioGroup
        // invisible until everything is loaded
        rg.visibility = View.INVISIBLE

        doAsync {
            try {
                jsonRanksArray =
                    JSONObject(URL("https://valorant-api.com/v1/competitivetiers").readText()).getJSONArray(
                        "data"
                    )
                val rankIndex = jsonRanksArray.length() - 1
                val jsonDetails = MatchHistoryActivity.matchJSON
                val matchData = jsonDetails.get("data") as JSONObject
                val allPlayers = matchData.getJSONObject("players").getJSONArray("all_players")

                for (i in 0 until allPlayers.length()) {
                    val currentPlayer = allPlayers.getJSONObject(i)
                    val stats = currentPlayer["stats"] as JSONObject
                    val assets = currentPlayer["assets"] as JSONObject
                    val agent = assets["agent"] as JSONObject
                    val playerName = "${currentPlayer["name"]}#${currentPlayer["tag"]}"
                    val playerTeam = currentPlayer["team"] as String
                    val playerLevel = currentPlayer.getString("level").toInt()
                    var playerRank = currentPlayer["currenttier_patched"] as String
                    val playerAgentURL = agent["small"] as String
                    val playerScore = stats.getString("score").toInt()
                    val playerKills = stats.getString("kills").toInt()
                    val playerDeaths = stats.getString("deaths").toInt()
                    val playerAssists = stats.getString("assists").toInt()
                    val playerCharacters = currentPlayer["character"] as String

                    var rankURL = ""
                    // Get the tier name from the API
                    val tier = currentPlayer.getInt("currenttier")
                    val tiers = jsonRanksArray.getJSONObject(rankIndex).getJSONArray("tiers")
                    for (j in 1 until tiers.length()) {
                        val currentTier = tiers[j] as JSONObject
                        if (currentTier.getInt("tier") == tier) {
                            rankURL = currentTier.getString("largeIcon")
                            break
                        }
                    }

                    // if the rankURL is empty, then try to get the tier name from the API using the getRank()
                    if (rankURL == "") {
                        val ranks = getRankURL(
                            currentPlayer["name"] as String,
                            currentPlayer["tag"] as String
                        )

                        if (ranks != "") {
                            rankURL = ranks.split("#")[0]
                            playerRank = ranks.split("#")[1]
                        }
                    }

                    val playerAgent = Agent(playerCharacters, playerAgentURL)
                    val playerRanks = Rank(playerRank, rankURL)
                    val player = Player(
                        playerName,
                        playerTeam,
                        playerLevel,
                        playerKills,
                        playerDeaths,
                        playerAssists,
                        playerScore,
                        playerAgent,
                        playerRanks
                    )
                    players.add(player)

                    uiThread {
                        val playerList: ListView = view.findViewById(R.id.playerList)
                        playerList.alpha = 0.3f
                        view.findViewById<TextView>(R.id.textView20).text =
                            "Processed ${players.size} players"
                        val playersAdapter = PlayersAdapter(requireActivity(), players)
                        playerList.adapter = playersAdapter
                        playersAdapter.notifyDataSetChanged()
                    }
                }

                uiThread {
                    displayPlayers("team")
                    rg.visibility = View.VISIBLE
                    rg.setOnCheckedChangeListener { _, checkedId ->
                        when (checkedId) {
                            R.id.teamsRadio -> displayPlayers("team")
                            R.id.namesRadio -> displayPlayers("name")
                            R.id.scoreRadio -> displayPlayers("score")
                            R.id.killsRadio -> displayPlayers("kills")
                            R.id.deathsRadio -> displayPlayers("deaths")
                            R.id.assistsRadio -> displayPlayers("assists")
                            R.id.levelsRadio -> displayPlayers("level")
                            R.id.ranksRadio -> displayPlayers("rank")
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


    private fun displayPlayers(type: String) {
        val playerList: ListView = view!!.findViewById(R.id.playerList)
        playerList.alpha = 1f

        // check what type is in the parameter and sort the players out accordingly
        when (type) {
            "kills" -> {
                players.sortByDescending { it.kills }
            }
            "deaths" -> {
                players.sortByDescending { it.deaths }
            }
            "assists" -> {
                players.sortByDescending { it.assists }
            }
            "score" -> {
                players.sortByDescending { it.score }
            }
            "level" -> {
                players.sortByDescending { it.level }
            }
            "name" -> {
                // sort names alphabetically regardless of capital letters
                players.sortBy { it.name.lowercase() }
            }
            "team" -> {
                players.sortBy { it.team }
            }
            "rank" -> {
                players.sortBy { it.rank.name }
            }
        }

        val playersAdapter = PlayersAdapter(requireActivity(), players)

        playerList.adapter = playersAdapter
        playersAdapter.notifyDataSetChanged()

        // find textview 20 and progressbar 5 and set the visibility to gone
        view!!.findViewById<TextView>(R.id.textView20).visibility = View.GONE
        view!!.findViewById<ProgressBar>(R.id.progressBar5).visibility = View.GONE

        playerList.setOnItemClickListener { _, _, position, _ ->
            val inflater =
                view!!.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.showplayerinfoval, null)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.MATCH_PARENT
            val focusable = true
            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
            val dimLayout = view!!.findViewById(R.id.dim_layout) as LinearLayout
            dimLayout.visibility = View.VISIBLE

            // get current Player from the list
            val currentPlayer = playersAdapter.getItem(position) as Player

            val playernameTitle = popupView.findViewById<TextView>(R.id.playerName)
            val playerImage: ImageView = popupView.findViewById(R.id.playerImage)
            val playerstats = popupView.findViewById<TextView>(R.id.playerstatsText)

            playernameTitle.text = currentPlayer.name
            Picasso.get().load(currentPlayer.agent.url).into(playerImage)
            val tier = currentPlayer.rank
            val level = currentPlayer.level
            val score = currentPlayer.score
            val kills = currentPlayer.kills
            val deaths = currentPlayer.deaths
            val assists = currentPlayer.assists

            if (tier.name == "Unrated") {
                playerstats.text = "Level: $level" +
                        "\nScore: $score" +
                        "\nKills: $kills" +
                        "\nDeaths: $deaths" +
                        "\nAssists: $assists"
            } else {
                playerstats.text = "Rank: ${tier.name}\nLevel: $level" +
                        "\nScore: $score" +
                        "\nKills: $kills" +
                        "\nDeaths: $deaths" +
                        "\nAssists: $assists"
            }
            val dismissbutton: Button = popupView.findViewById(R.id.dismiss)
            dismissbutton.setOnClickListener {
                popupWindow.dismiss()
                dimLayout.visibility = View.INVISIBLE
            }

            val copyButton: Button = popupView.findViewById(R.id.copyPlayerName)
            copyButton.setOnClickListener {
                copyText(currentPlayer.name)
                Toast.makeText(requireActivity(), "Copied Name!", Toast.LENGTH_SHORT)
                    .show()
            }

            val rankButton: Button = popupView.findViewById(R.id.playerRankButon)
            rankButton.setOnClickListener {
                val name = currentPlayer.getNameAndTag()
                val intent = Intent(requireActivity(), MMRActivity::class.java)
                intent.putExtra("RiotName", name.first)
                intent.putExtra("RiotID", name.second)
                startActivity(intent)
            }
        }
    }

    private fun getRankURL(name: String, tag: String): String {
        try {
            var rankURL = ""
            var rankName = ""
            // attempt to get all of the players rank in unrated or other modes
            val tierRanking =
                henrikAPI("https://api.henrikdev.xyz/valorant/v1/mmr/eu/$name/$tag").getJSONObject("data")
                    .getInt("currenttier")
            val rankIndex = jsonRanksArray.length() - 1
            val tiers = jsonRanksArray.getJSONObject(rankIndex).getJSONArray("tiers")
            for (j in 0 until tiers.length()) {
                val currentTier = tiers[j] as JSONObject
                if (currentTier.getInt("tier") == tierRanking) {
                    rankURL = currentTier.getString("largeIcon")
                    rankName = currentTier.getString("tierName")
                    break
                }
            }
            //Log.d("Henrik", "RankURL for $name#$tag: $rankURL")
            return "$rankURL#$rankName"
        } catch (e: Exception) {
            //Log.d("Henrik", "Error for $name#$tag - $e")
            return ""
        }
    }

    private fun copyText(text: String) {
        val myClipboard = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val myClip: ClipData = ClipData.newPlainText("Label", text)
        myClipboard.setPrimaryClip(myClip)
    }

    private fun henrikAPI(playerURL: String): JSONObject {
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
