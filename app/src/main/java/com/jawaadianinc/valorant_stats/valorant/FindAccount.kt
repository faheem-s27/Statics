package com.jawaadianinc.valorant_stats.valorant

import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.net.URL

class FindAccount : AppCompatActivity() {
    private lateinit var imagebackground: ImageView
    private val imagesURL = java.util.ArrayList<String>()
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    val playerName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaccount)
        imagesURL.add("https://media.valorant-api.com/playercards/3432dc3d-47da-4675-67ae-53adb1fdad5e/largeart.png")
        doAsync {
            val getValoImagesURL =
                JSONObject(URL("https://valorant-api.com/v1/playercards").readText())
            val images = getValoImagesURL["data"] as JSONArray
            for (i in 0 until images.length()) {
                val imageURL = images[i] as JSONObject
                imagesURL.add(imageURL["largeArt"].toString())
            }
        }

        val findaccountButton: Button = findViewById(R.id.generalStats)
        val MMR: Button = findViewById(R.id.MMR)
        val matchHistoryButton: Button = findViewById(R.id.matchHistory)
        val updatesButton: Button = findViewById(R.id.updateBT)
        val compareButton: Button = findViewById(R.id.compareBT)
        val viewMatch: Button = findViewById(R.id.viewHistory)
        val leaderBoard: Button = findViewById(R.id.leaderboard)
        val recentPlayers: Button = findViewById(R.id.recentPlayers)
        val refreshPlayer: ImageButton = findViewById(R.id.refreshUpdate)

        imagebackground = findViewById(R.id.imagebackground)
        Picasso.get().load(imagesURL.random()).into(imagebackground)
        syncFireBase()

        val name = PlayerDatabase(this).getPlayerSignedIn()
        if (name == null) {
            Toast.makeText(this, "Sign in to continue!", Toast.LENGTH_SHORT).show()
            MMR.hide()
            matchHistoryButton.hide()
            updatesButton.hide()
            compareButton.hide()
            viewMatch.hide()
            recentPlayers.hide()
            refreshPlayer.hide()
            findViewById<TextView>(R.id.textView3).hide()
            findViewById<TextView>(R.id.textView6).hide()
            findViewById<TextView>(R.id.textView7).hide()
            findViewById<Button>(R.id.leaderboard).hide()
            findViewById<Button>(R.id.splitscreenButton).hide()
        }

        matchHistoryButton.setOnClickListener {
//            if (mySpinner.selectedItem != null) {
//                findMatches(mySpinner.selectedItem.toString())
//            }
        }

        findaccountButton.setOnClickListener {
            //Logging into Riot Client
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://auth.riotgames.com/authorize?client_id=statics&redirect_uri=https://statics-fd699.web.app/authorize.html&response_type=code&scope=openid+offline_access")
                )
            )
        }

        updatesButton.setOnClickListener {
            startActivity(Intent(this, ValorantUpdatesActivity::class.java))
        }

        findViewById<Button>(R.id.splitscreenButton).setOnClickListener {
            compareStats()
        }

        compareButton.setOnClickListener {
            Toast.makeText(this, "Coming soon!", Toast.LENGTH_SHORT).show()
        }

        MMR.setOnClickListener {
//            val fullname = mySpinner.selectedItem.toString()
//            val name = fullname.split("#")
//            val intent = Intent(this@FindAccount, MMRActivity::class.java)
//            intent.putExtra("RiotName", name[0])
//            intent.putExtra("RiotID", name[1])
//            startActivity(intent)
        }

        leaderBoard.setOnClickListener {
            startActivity(Intent(this, leaderBoardActivity::class.java))
        }

        viewMatch.setOnClickListener {
//            val intent = Intent(this@FindAccount, ViewMatches::class.java)
//            intent.putExtra("RiotName", mySpinner.selectedItem.toString())
//            startActivity(intent)
        }

        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            doTask(handler)
        }
        handler.postDelayed(runnable, 500)

//        recentPlayers.setOnClickListener {
//            val contextView = findViewById<View>(R.id.playerLevel)
//            val snackbar = Snackbar
//                .make(contextView, "Looking for recent players. Wait...", Snackbar.LENGTH_LONG)
//            snackbar.show()
//            val fullName = mySpinner.selectedItem.toString()
//            val split = fullName.split("#")
//            val matchHistoryURL =
//                "https://api.henrikdev.xyz/valorant/v3/matches/eu/${split[0]}/${split[1]}?size=10"
//            doAsync {
//                try {
//                    val json = JSONObject(URL(matchHistoryURL).readText())
//                    val data = json["data"] as JSONArray
//                    val players: ArrayList<String> = ArrayList()
//                    var filterMode = arrayOf("")
//                    for (i in 0 until data.length()) {
//                        val allPlayers = data.getJSONObject(i).getJSONObject("players")
//                            .getJSONArray("all_players") as JSONArray
//                        for (j in 0 until allPlayers.length()) {
//                            players += allPlayers.getJSONObject(j)
//                                .getString("name") + "#" + allPlayers.getJSONObject(j)
//                                .getString("tag")
//                        }
//                    }
//                    for (player in players) {
//                        if (!filterMode.contains(player)) {
//                            filterMode += player
//                        }
//                    }
//                    val mapofPlayerOccurences: MutableMap<String, Int> = mutableMapOf("Name" to 10)
//                    val finalName: String? = null
//                    for (player in filterMode) {
//                        val occurrences = Collections.frequency(players, player)
//                        if (player != fullName) {
//                            if (occurrences > 1) {
//                                mapofPlayerOccurences[player] = occurrences
//                            }
//                        }
//                    }
//
//                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                        mapofPlayerOccurences.remove("Name", 10)
//                    }
//                    val result =
//                        mapofPlayerOccurences.toList().sortedBy { (_, value) -> value }.toMap()
//                    var finalList = arrayOf("")
//                    val list = result.toList()
//                    list.forEach {
//                        finalList += it.first + " in ${it.second} games"
//                    }
//                    finalList.reverse()
//                    finalList = finalList.filter { x: String? -> x != "" }.toTypedArray()
//                    if (finalList.isNotEmpty()) {
//                        uiThread {
//                            val builder = AlertDialog.Builder(this@FindAccount)
//                            builder.setTitle("Found ${filterMode.count()} players in last 10 games!")
//                            builder.setItems(finalList,
//                                DialogInterface.OnClickListener { _, itemIndex ->
//                                    addNameToList(finalList[itemIndex])
//                                })
//                            val dialog = builder.create()
//                            dialog.window!!.attributes.windowAnimations =
//                                R.style.DialogAnimation_2
//                            dialog.show()
//                        }
//                    } else {
//                        val snackbar = Snackbar
//                            .make(contextView, "No repeating players found!", Snackbar.LENGTH_SHORT)
//                        snackbar.show()
//                    }
//                } catch (e: FileNotFoundException) {
//                    val snackbar = Snackbar
//                        .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
//                    snackbar.show()
//                } catch (e: Exception) {
//                    Log.d("players", e.toString())
//                    val snackbar = Snackbar
//                        .make(contextView, "Error occurred!", Snackbar.LENGTH_LONG)
//                    snackbar.show()
//                }
//            }
//        }
    }

    private fun doTask(handler: Handler) {
        Picasso.get().load(imagesURL.random()).placeholder(imagebackground.drawable)
            .into(imagebackground)
        handler.postDelayed(runnable, 3000)

    }

//    private fun updatePlayerUI() {
//        try {
//            val mySpinner = findViewById<View>(R.id.spinner) as Spinner
//            val playerProfile: ImageView = findViewById(R.id.playerProfile)
//            val playerLevel: TextView = findViewById(R.id.playerLevel)
//            val playerName: TextView = findViewById(R.id.playerNameMenu)
//            val fullname = mySpinner.selectedItem.toString()
//            val name = fullname.split("#")
//            //Toast.makeText(this, player.toString(), Toast.LENGTH_SHORT).show()
//
//            doAsync {
//                try {
//                    if (fullname.isNotBlank()) {
//                        uiThread {
//                            val database = Firebase.database
//                            val playersRef = database.getReference("VALORANT/players")
//                            playersRef.child(name[0]).child("Avatar")
//                                .addListenerForSingleValueEvent(object : ValueEventListener {
//                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                        try {
//                                            val avatar = dataSnapshot.value as String
//                                            Picasso.get().load(avatar).fit().centerInside()
//                                                .into(playerProfile)
//                                        } catch (e: Exception) {
//                                            doAsync {
//                                                val data =
//                                                    JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true").readText())["data"] as JSONObject
//                                                val database = Firebase.database
//                                                val playersRef =
//                                                    database.getReference("VALORANT/players")
//                                                playersRef.child(name[0]).child("Tag")
//                                                    .setValue(name[1])
//                                                playersRef.child(name[0]).child("Avatar")
//                                                    .setValue(
//                                                        data.getJSONObject("card").get("small")
//                                                    )
//                                                playersRef.child(name[0]).child("AvatarLarge")
//                                                    .setValue(
//                                                        data.getJSONObject("card").get("large")
//                                                    )
//                                                playersRef.child(name[0]).child("Level")
//                                                    .setValue(data.get("account_level"))
//                                                uiThread {
//                                                    updatePlayerUI()
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    override fun onCancelled(databaseError: DatabaseError) {
//                                    }
//                                })
//                            playersRef.child(name[0]).child("Level")
//                                .addListenerForSingleValueEvent(object : ValueEventListener {
//                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                        try {
//                                            playerLevel.text =
//                                                (dataSnapshot.value as Long).toString()
//                                        } catch (e: Exception) {
//                                            doAsync {
//                                                val data =
//                                                    JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true").readText())["data"] as JSONObject
//                                                val database = Firebase.database
//                                                val playersRef =
//                                                    database.getReference("VALORANT/players")
//                                                playersRef.child(name[0]).child("Tag")
//                                                    .setValue(name[1])
//                                                playersRef.child(name[0]).child("Avatar")
//                                                    .setValue(
//                                                        data.getJSONObject("card").get("small")
//                                                    )
//                                                playersRef.child(name[0]).child("AvatarLarge")
//                                                    .setValue(
//                                                        data.getJSONObject("card").get("large")
//                                                    )
//                                                playersRef.child(name[0]).child("Level")
//                                                    .setValue(data.get("account_level"))
//                                                uiThread {
//                                                    updatePlayerUI()
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    override fun onCancelled(databaseError: DatabaseError) {
//                                    }
//                                })
//                            playersRef.child(name[0]).child("AvatarLarge")
//                                .addListenerForSingleValueEvent(object : ValueEventListener {
//                                    override fun onDataChange(dataSnapshot: DataSnapshot) {
//                                        try {
//                                            Picasso.get().load(dataSnapshot.value.toString())
//                                                .into(imagebackground)
//                                        } catch (e: Exception) {
//                                            doAsync {
//                                                val data =
//                                                    JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true").readText())["data"] as JSONObject
//                                                val database = Firebase.database
//                                                val playersRef =
//                                                    database.getReference("VALORANT/players")
//                                                playersRef.child(name[0]).child("Tag")
//                                                    .setValue(name[1])
//                                                playersRef.child(name[0]).child("Avatar")
//                                                    .setValue(
//                                                        data.getJSONObject("card").get("small")
//                                                    )
//                                                playersRef.child(name[0]).child("AvatarLarge")
//                                                    .setValue(
//                                                        data.getJSONObject("card").get("large")
//                                                    )
//                                                playersRef.child(name[0]).child("Level")
//                                                    .setValue(data.get("account_level"))
//                                                uiThread {
//                                                    updatePlayerUI()
//                                                }
//                                            }
//                                        }
//                                    }
//
//                                    override fun onCancelled(databaseError: DatabaseError) {
//                                    }
//                                })
//
//                            playerName.text = mySpinner.selectedItem.toString()
//                        }
//                    }
//                } catch (e: Exception) {
//                    Log.d("test", e.toString())
//                }
//            }
//        } catch (E: Exception) {
//            Toast.makeText(this, "Get started by typing your VALORANT name!", Toast.LENGTH_SHORT)
//                .show()
//        }
//
//    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    private fun addNameToList(name: String) {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Adding User")
        progressDialog.setMessage("Collecting details.")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.show()
        val cropName = name.split(" in")
        val name = cropName[0].split("#")
        doAsync {
            val data =
                JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true").readText())["data"] as JSONObject
            val database = Firebase.database
            val playersRef = database.getReference("VALORANT/players")
            playersRef.child(name[0]).child("Tag").setValue(name[1])
            playersRef.child(name[0]).child("Avatar")
                .setValue(data.getJSONObject("card").get("small"))
            playersRef.child(name[0]).child("AvatarLarge")
                .setValue(data.getJSONObject("card").get("large"))
            playersRef.child(name[0]).child("Level").setValue(data.get("account_level"))

            val file = File(this@FindAccount.filesDir, "texts")
            if (!file.exists()) {
                file.mkdir()
            }
            uiThread {
                try {
                    val gpxfile = File(file, "Players")
                    if (!gpxfile.exists()) {
                        gpxfile.createNewFile()
                    }
                    var pass = true
                    gpxfile.forEachLine {
                        if (it == cropName[0]) {
                            val contextView = findViewById<View>(R.id.generalStats)
                            progressDialog.dismiss()
                            val snackbar = Snackbar
                                .make(
                                    contextView,
                                    "User already added!",
                                    Snackbar.LENGTH_LONG
                                )
                            snackbar.show()
                            pass = false
                        }
                    }

                    if (pass) {
                        if (gpxfile.readText() == "") {
                            gpxfile.appendText(cropName[0])
                        } else {
                            gpxfile.appendText("\n" + cropName[0])
                        }
                        Toast.makeText(this@FindAccount, "Saved!", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                        refresh()
                    }
                } catch (e: java.lang.Exception) {
                    progressDialog.dismiss()
                    Toast.makeText(this@FindAccount, "Error: $e", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun findMatches(RiotName: String){
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Fetching Matches")
        progressDialog.setMessage("Please wait a moment")
        progressDialog.setCancelable(false)
        var name = ""

        name = when (RiotName) {
            "1" -> {
                "SprinkledRainbow#1593"
            }
            "2" -> {
                "Slayzerzz#1169"
            }
            "3" -> {
                "AwesomeGamer#4100"
            }
            else -> {
                RiotName
            }
        }

        val nameSplit = name.split("#")
        val Name = nameSplit[0]
        val ID = nameSplit[1]
        if (isNetworkAvailable()) {
            progressDialog.show()
            val matchHistoryURL =
                "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID?size=10"

            doAsync {
                try {
                    //find Matches
                    try {
                        val matchhistoryURL = URL(matchHistoryURL).readText()
                        val jsonMatches = JSONObject(matchhistoryURL)
                        val statsus = jsonMatches.getInt("status")
                        if (statsus == 200) {
                            val data = jsonMatches["data"] as JSONArray
                            val matches: MutableList<String> = ArrayList()

                            for (i in 0 until data.length()) {
                                val map =
                                    data.getJSONObject(i).getJSONObject("metadata").getString("map")
                                val mode = data.getJSONObject(i).getJSONObject("metadata")
                                    .getString("mode")
                                matches.add("\n$mode on $map")
                                val matchID = data.getJSONObject(i).getJSONObject("metadata")
                                    .getString("matchid")
                                val database = Firebase.database
                                val playersRef = database.getReference("VALORANT/players")
                                playersRef.child(Name).child("Tag").setValue(ID)
                                playersRef.child(Name).child("Matches").child(matchID).child("Map")
                                    .setValue(map)
                                playersRef.child(Name).child("Matches").child(matchID).child("Mode")
                                    .setValue(mode)

                                val matchDatabse = MatchDatabases(this@FindAccount)
                                matchDatabse.addMatches(
                                    matchID,
                                    RiotName,
                                    map,
                                    mode
                                )
                            }
                            uiThread {
                                syncFireBase()
                                val builder = AlertDialog.Builder(this@FindAccount)
                                builder.setTitle("Here are the last 10 matches!")
                                builder.setItems(
                                    arrayOf<CharSequence>(
                                        matches[0],
                                        matches[1],
                                        matches[2],
                                        matches[3],
                                        matches[4],
                                        matches[5],
                                        matches[6],
                                        matches[7],
                                        matches[8],
                                        matches[9]
                                    )
                                ) { _, which ->
                                    when (which) {
                                        0 -> matchActivityStart(
                                            Name,
                                            ID,
                                            0,
                                            data.getJSONObject(0).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        1 -> matchActivityStart(
                                            Name,
                                            ID,
                                            1,
                                            data.getJSONObject(1).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        2 -> matchActivityStart(
                                            Name,
                                            ID,
                                            2,
                                            data.getJSONObject(2).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        3 -> matchActivityStart(
                                            Name,
                                            ID,
                                            3,
                                            data.getJSONObject(3).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        4 -> matchActivityStart(
                                            Name,
                                            ID,
                                            4,
                                            data.getJSONObject(4).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        5 -> matchActivityStart(
                                            Name,
                                            ID,
                                            5,
                                            data.getJSONObject(5).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        6 -> matchActivityStart(
                                            Name,
                                            ID,
                                            6,
                                            data.getJSONObject(6).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        7 -> matchActivityStart(
                                            Name,
                                            ID,
                                            7,
                                            data.getJSONObject(7).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        8 -> matchActivityStart(
                                            Name,
                                            ID,
                                            8,
                                            data.getJSONObject(8).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                        9 -> matchActivityStart(
                                            Name,
                                            ID,
                                            9,
                                            data.getJSONObject(9).getJSONObject("metadata")
                                                .getString("matchid")
                                        )
                                    }
                                }
                                progressDialog.dismiss()
                                val dialog = builder.create()
                                dialog.window!!.attributes.windowAnimations =
                                    R.style.DialogAnimation_2
                                dialog.show()
                            }
                        }
                        else{
                            uiThread {
                                progressDialog.dismiss()
                                AlertDialog.Builder(this@FindAccount).setTitle("Server Error!")
                                    .setMessage("Cannot connect to server at the moment :(")
                                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                                    .setIcon(android.R.drawable.ic_dialog_alert).show()
                            }
                        }

                    } catch (e: FileNotFoundException) {
                        uiThread {
                            progressDialog.dismiss()
                            AlertDialog.Builder(this@FindAccount).setTitle("Server Error!")
                                .setMessage("This seems to be a server error which will be fixed soon!")
                                .setPositiveButton(android.R.string.ok) { _, _ -> }
                                .setIcon(android.R.drawable.ic_dialog_alert).show()
                        }
                    } catch (e: Exception) {
                        uiThread {
                            progressDialog.dismiss()
                            AlertDialog.Builder(this@FindAccount).setTitle("Error!")
                                .setMessage("Error Message: $e")
                                .setPositiveButton(android.R.string.ok) { _, _ -> }
                                .setIcon(android.R.drawable.ic_dialog_alert).show()
                        }
                    }


                } catch (e: FileNotFoundException) {
                    uiThread {
                        progressDialog.dismiss()
                        val contextView = findViewById<View>(R.id.generalStats)
                        val snackbar = Snackbar
                            .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
                        snackbar.show()
                    }
                } catch (e: Exception) {
                    uiThread {
                        progressDialog.dismiss()
                        AlertDialog.Builder(this@FindAccount).setTitle("Error!")
                            .setMessage("Error Message: $e")
                            .setPositiveButton(android.R.string.ok) { _, _ -> }
                            .setIcon(android.R.drawable.ic_dialog_alert).show()
                    }
                }
            }

        }
        else {
            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "No internet connection!", Snackbar.LENGTH_LONG)
            snackbar.show()
        }

    }

    private fun matchActivityStart(Name: String, ID: String, matchNumber: Int, matchID: String) {
        val matchintent = Intent(this@FindAccount, MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", matchNumber)
        matchintent.putExtra("MatchID", matchID)
        startActivity(matchintent)
    }

    private fun compareStats() {
        startActivity(Intent(this, CompareActivity::class.java))
    }

    private fun refresh() {
        finish()
        startActivity(Intent(this, FindAccount::class.java))
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }

    private fun syncFireBase() {
        val matchDatabse = MatchDatabases(this@FindAccount)
        try {
            val userList = java.util.ArrayList<String>()
            val sqlString =
                "SELECT ${MatchDatabases.USER} FROM ${MatchDatabases.USERMATCHES}"
            val db = matchDatabse.readableDatabase
            val cursor = db.rawQuery(sqlString, null)
            if (cursor.moveToFirst()) do {
                val users = cursor.getString(0)
                if (!userList.contains(users)) {
                    userList.add(users)
                }
            } while (cursor.moveToNext())
            val database = Firebase.database
            val playersRef = database.getReference("VALORANT/players")
            cursor.close()
            db.close()

            for (user in userList) {
                val userFinal = user.split("#")
                val newString =
                    "SELECT ${MatchDatabases.MATCH_ID}, ${MatchDatabases.MAP}, ${MatchDatabases.GAMEMODE} FROM ${MatchDatabases.USERMATCHES} WHERE ${MatchDatabases.USER} = '${user}'"
                val db = matchDatabse.readableDatabase
                val matchDCursor = db.rawQuery(newString, null)
                if (matchDCursor.moveToFirst()) do {
                    playersRef.child(userFinal[0]).child("Tag").setValue(userFinal[1])
                    playersRef.child(userFinal[0]).child("Matches").child(matchDCursor.getString(0))
                        .child("Map").setValue(matchDCursor.getString(1))
                    playersRef.child(userFinal[0]).child("Matches").child(matchDCursor.getString(0))
                        .child("Mode").setValue(matchDCursor.getString(2))
                } while (matchDCursor.moveToNext())
                matchDCursor.close()
                db.close()
            }

        } catch (e: Exception) {
            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "Database not synced!", Snackbar.LENGTH_SHORT)
            snackbar.show()
            Log.d("database", e.toString())
        }
    }
}

private fun View.hide() {
    this.visibility = View.INVISIBLE
}
