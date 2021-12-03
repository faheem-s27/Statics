package com.jawaadianinc.valorant_stats

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
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
import java.io.InputStream
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
                val matchdetailsURL = URL(matchURl).readText()
                val jsonDetails = JSONObject(matchdetailsURL)
                val matchData = jsonDetails["data"] as JSONObject

                val gridview: GridView = view.findViewById(R.id.redGridview)
                val bluegridview: GridView = view.findViewById(R.id.blueGridView)

                val arrayList = ArrayList<String>()
                val mAdapter = object :
                    ArrayAdapter<String?>(
                        activity?.applicationContext!!, android.R.layout.simple_expandable_list_item_1,
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

                val bluelist = ArrayList<String>()
                val blueAdapter = object :
                    ArrayAdapter<String?>(
                        activity?.applicationContext!!, android.R.layout.simple_expandable_list_item_1,
                        bluelist as List<String?>
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
                    gridview.adapter = mAdapter
                    bluegridview.adapter = blueAdapter
                }

                val players = matchData.getJSONObject("players")
                val redplayers = players.getJSONArray("red")
                val blueplayers = players.getJSONArray("blue")
                val allPlayers = players.getJSONArray("all_players")

                var playerNames: Array<String> = arrayOf()
                var agentURLs: Array<String> = arrayOf()

                for (i in 0 until allPlayers.length()) {
                    val player = allPlayers[i] as JSONObject
                    val assets = player["assets"] as JSONObject
                    val agent = assets["agent"] as JSONObject
                    playerNames += (player.getString("name"))
                    agentURLs += agent.getString("small")
                }

                var drawAble: IntArray = intArrayOf()

                for (i in agentURLs) {
                    uiThread {
                        //Toast.makeText(activity?.applicationContext!!, i, Toast.LENGTH_SHORT).show()
                    }
                }

                //val mainAdapter = PlayerAdapter(activity?.applicationContext!!, playerNames, drawAble)
                //gridView.setAdapter(mainAdapter)

                var redplayerCodes: Array<String> = arrayOf()
                var blueplayerCodes: Array<String> = arrayOf()

                for (i in 0 until redplayers.length()) {
                    val player = redplayers[i] as JSONObject
                    val name = player.getString("name")
                    val agent = player.getString("character")
                    val score = player.getJSONObject("stats").getString("score")
                    val level = player.getString("level")
                    redplayerCodes += (player.getString("player_title"))
                    uiThread {
                        mAdapter.add("$name - Level $level")
                    }
                }

                var redtitlelist: Array<String> = arrayOf()
                for (code in redplayerCodes) {
                    doAsync {
                        try {
                            val urltogetTitle =
                                "https://valorant-api.com/v1/playertitles/$code"
                            val url = URL(urltogetTitle).readText()
                            val json = JSONObject(url)
                            redtitlelist += json.getJSONObject("data").getString("titleText")
                        } catch (e: Exception) {
                            Toast.makeText(requireActivity(), "Error: $e", Toast.LENGTH_SHORT)
                                .show()
                        }
                    }
                }


                for (i in 0 until blueplayers.length()){
                    val player = blueplayers[i] as JSONObject
                    val name = player.getString("name")
                    val agent = player.getString("character")
                    val level = player.getString("level")
                    val score = player.getJSONObject("stats").getString("score")
                    blueplayerCodes += (player.getString("player_title"))
                    uiThread {
                        blueAdapter.add("$name - Level $level")
                    }
                }

                var bluetitlelist : Array<String> = arrayOf()
                for (code in blueplayerCodes){
                    doAsync {
                        try {
                            val urltogetTitle =
                                "https://valorant-api.com/v1/playertitles/$code"
                            val url = URL(urltogetTitle).readText()
                            val json = JSONObject(url)
                            bluetitlelist += json.getJSONObject("data").getString("titleText")
                        } catch (e: Exception) {
                            Toast.makeText(requireActivity(), "Error: $e", Toast.LENGTH_SHORT).show()
                        }
                    }
                }


                uiThread {
                    gridview.setOnItemClickListener { _, _, position, _ ->
                        val inflater =
                            view.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val popupView: View = inflater.inflate(R.layout.showredplayer, null)
                        val width = LinearLayout.LayoutParams.MATCH_PARENT
                        val height = LinearLayout.LayoutParams.MATCH_PARENT
                        val focusable = true
                        val popupWindow = PopupWindow(popupView, width, height, focusable)
                        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

                        val playernameTitle = popupView.findViewById<TextView>(R.id.playerName)
                        val playerrDetails = redplayers[position] as JSONObject
                        playernameTitle.text = playerrDetails.getString("name") + "#" + playerrDetails.getString("tag")

                        val playerImage : ImageView = popupView.findViewById(R.id.playerImage)
                        val playerImageCode = playerrDetails.getString("player_card")
                        val imageURL = "https://media.valorant-api.com/playercards/$playerImageCode/displayicon.png"
                        Picasso.get().load(imageURL).into(playerImage)


                        val playerstats = popupView.findViewById<TextView>(R.id.playerstatsText)
                        val tier = playerrDetails.getString("currenttier_patched")
                        val agent = playerrDetails.getString("character")
                        val score = playerrDetails.getJSONObject("stats").getString("score")
                        val kills = playerrDetails.getJSONObject("stats").getString("kills")
                        val deaths =playerrDetails.getJSONObject("stats").getString("deaths")
                        val assists = playerrDetails.getJSONObject("stats").getString("assists")
                        val level = playerrDetails.getString("level")

                        popupView.findViewById<TextView>(R.id.playertitle).text = redtitlelist[position]

                        playerstats.text = "Rank: $tier" +
                                "\nLevel: $level" +
                                "\nAgent: $agent" +
                                "\nScore: $score" +
                                "\nKills: $kills" +
                                "\nDeaths: $deaths" +
                                "\nAssists: $assists"


                        val dismissbutton : Button = popupView.findViewById(R.id.dismiss)
                        dismissbutton.setOnClickListener{
                            popupWindow.dismiss()
                        }

                        val copyButton : Button = popupView.findViewById(R.id.copyPlayerName)
                        copyButton.setOnClickListener{
                            copyText(playernameTitle.text.toString())
                            Toast.makeText(requireActivity(), "Copied Name!", Toast.LENGTH_SHORT).show()
                        }

                    }

                    bluegridview.setOnItemClickListener { _, _, position, _ ->
                        val inflater =
                            view.context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
                        val popupView: View = inflater.inflate(R.layout.showredplayer, null)
                        val width = LinearLayout.LayoutParams.MATCH_PARENT
                        val height = LinearLayout.LayoutParams.MATCH_PARENT
                        val focusable = true
                        val popupWindow = PopupWindow(popupView, width, height, focusable)
                        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0)

                        val playernameTitle = popupView.findViewById<TextView>(R.id.playerName)
                        val playerrDetails = blueplayers[position] as JSONObject
                        playernameTitle.text = playerrDetails.getString("name") + "#" + playerrDetails.getString("tag")

                        val playerImage : ImageView = popupView.findViewById(R.id.playerImage)
                        val playerImageCode = playerrDetails.getString("player_card")
                        val imageURL = "https://media.valorant-api.com/playercards/$playerImageCode/displayicon.png"
                        Picasso.get().load(imageURL).into(playerImage)

                        val playerstats = popupView.findViewById<TextView>(R.id.playerstatsText)
                        val tier = playerrDetails.getString("currenttier_patched")
                        val agent = playerrDetails.getString("character")
                        val score = playerrDetails.getJSONObject("stats").getString("score")
                        val kills = playerrDetails.getJSONObject("stats").getString("kills")
                        val deaths =playerrDetails.getJSONObject("stats").getString("deaths")
                        val assists = playerrDetails.getJSONObject("stats").getString("assists")
                        val level = playerrDetails.getString("level")

                        playerstats.text = "Rank: $tier" +
                                "\nLevel: $level" +
                                "\nAgent: $agent" +
                                "\nScore: $score" +
                                "\nKills: $kills" +
                                "\nDeaths: $deaths" +
                                "\nAssists: $assists"
                        popupView.findViewById<TextView>(R.id.playertitle).text = bluetitlelist[position]

                        val dismissbutton : Button = popupView.findViewById(R.id.dismiss)
                        dismissbutton.setOnClickListener{
                            popupWindow.dismiss()
                        }

                        val copyButton : Button = popupView.findViewById(R.id.copyPlayerName)
                        copyButton.setOnClickListener{
                            copyText(playernameTitle.text.toString())
                            Toast.makeText(requireActivity(), "Copied Name!", Toast.LENGTH_SHORT).show()
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

    fun LoadImageFromWebURL(url: String?): Drawable? {
        return try {
            val iStream: InputStream = URL(url).content as InputStream
            Drawable.createFromStream(iStream, "AgentURL")
        } catch (e: java.lang.Exception) {
            null
        }
    }

}