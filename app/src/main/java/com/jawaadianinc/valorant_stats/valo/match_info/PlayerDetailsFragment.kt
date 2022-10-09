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
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.activities.MMRActivity
import com.jawaadianinc.valorant_stats.valo.adapters.PlayersAdapter
import com.jawaadianinc.valorant_stats.valo.classes.Agent
import com.jawaadianinc.valorant_stats.valo.classes.Player
import com.jawaadianinc.valorant_stats.valo.classes.Rank
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

    private var jsonRanksArray = JSONArray()
    private val players = ArrayList<Player>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val rg = view.findViewById(R.id.radioGroup) as RadioGroup
        // invisible until everything is loaded
        rg.visibility = View.INVISIBLE

        val playerList: ListView = view.findViewById(R.id.playerList)
        val playersAdapter = PlayersAdapter(requireActivity(), players)
        playerList.adapter = playersAdapter
        playerList.alpha = 0.3f

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
                        view.findViewById<TextView>(R.id.textView20).text =
                            "Processed ${players.size} players"
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
                            R.id.latestRadio -> displayPlayers("score")
                            R.id.killsRadio -> displayPlayers("kills")
                            R.id.deathsRadio -> displayPlayers("deaths")
                            R.id.assistsRadio -> displayPlayers("assists")
                            R.id.levelsRadio -> displayPlayers("level")
                            R.id.compRadio -> displayPlayers("rank")
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
        // check if activity is still alive
        if (activity == null) return

        val playerList: ListView = requireView().findViewById(R.id.playerList)
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
        requireView().findViewById<TextView>(R.id.textView20).visibility = View.GONE
        requireView().findViewById<ProgressBar>(R.id.progressBar5).visibility = View.GONE

        playerList.setOnItemClickListener { _, _, position, _ ->
            val inflater =
                requireView().context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView: View = inflater.inflate(R.layout.showplayerinfoval, null)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.MATCH_PARENT
            val focusable = true
            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)
            val dimLayout = requireView().findViewById(R.id.dim_layout) as LinearLayout

            // set the dim layout to alpha 1f in 500ms
            dimLayout.animate().alpha(1f).setDuration(500).start()


            // set the popup window to dismiss when the back button is pressed
            popupWindow.setOnDismissListener {
                dimLayout.animate().alpha(0f).setDuration(500).start()
                popupView.animate().alpha(0f).setDuration(500).withEndAction {
                    popupWindow.dismiss()
                }.start()
            }

            popupView.alpha = 0f
            popupView.animate().alpha(1f).setDuration(500).start()


            // get current Player from the list
            val currentPlayer = playersAdapter.getItem(position) as Player

            val playernameTitle = popupView.findViewById<TextView>(R.id.playerName)
            val playerImage: ImageView = popupView.findViewById(R.id.playerImage)
            val playerstats = popupView.findViewById<TextView>(R.id.playerstatsText)
            val dismissbutton: Button = popupView.findViewById(R.id.dismiss)
            val copyButton: Button = popupView.findViewById(R.id.copyPlayerName)
            val rankButton: Button = popupView.findViewById(R.id.playerRankButon)

            val animationLength = 500L

            // move the player image to the top of the screen and then animate it back to the center
            playerImage.animate().translationY(-1000f).setDuration(0).alpha(0f).start()
            playerImage.animate().translationY(0f).setDuration(animationLength).alpha(1f)
                .setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.start()

            // move the player name to the left of the screen and then animate it back to the center
            playernameTitle.animate().translationX(-1000f).setDuration(0).alpha(0f).start()
            playernameTitle.animate().translationX(0f).setDuration(animationLength).alpha(1f)
                .setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.startDelay = 100

            // move the player stats to the right of the screen and then animate it back to the center
            playerstats.animate().translationX(1000f).setDuration(0).alpha(0f).start()
            playerstats.animate().translationX(0f).setDuration(animationLength).alpha(1f)
                .setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.startDelay = 300

            // move the dismiss button to the bottom of the screen and then animate it back to the center
            dismissbutton.animate().translationY(1000f).setDuration(0).alpha(0f).start()
            dismissbutton.animate().translationY(0f).setDuration(animationLength).alpha(1f)
                .setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.startDelay = 500

            // move the copy button to the bottom of the screen and then animate it back to the center
            copyButton.animate().translationY(1000f).setDuration(0).alpha(0f).start()
            copyButton.animate().translationY(0f).setDuration(animationLength).alpha(1f)
                .setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.startDelay = 300

            // move the rank button to the bottom of the screen and then animate it back to the center
            rankButton.animate().translationY(1000f).setDuration(0).alpha(0f).start()
            rankButton.animate().translationY(0f).setDuration(animationLength).alpha(1f)
                .setInterpolator {
                    val t = it - 1.0f
                    t * t * t * t * t + 1.0f
                }.startDelay = 400

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

            dismissbutton.setOnClickListener {
                popupView.animate().alpha(0f).setDuration(500).withEndAction {
                    popupWindow.dismiss()
                }.start()
                dimLayout.animate().alpha(0f).setDuration(500).start()
            }


            copyButton.setOnClickListener {
                copyText(currentPlayer.name)
                Toast.makeText(requireActivity(), "Copied Name!", Toast.LENGTH_SHORT)
                    .show()
            }

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
            // check if activity is not null
            if (activity != null) {
                val tierRanking =
                    Henrik(requireActivity()).henrikAPI("https://api.henrikdev.xyz/valorant/v1/mmr/eu/$name/$tag")
                        .getJSONObject("data")
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
            } else {
                return ""
            }
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
}
