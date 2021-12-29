package com.jawaadianinc.valorant_stats

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.FileNotFoundException
import java.net.URL
import java.util.*
import kotlin.collections.ArrayList


class FindAccount : AppCompatActivity() {
    var PLAYERCARD = ""
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var imagebackground: ImageView

    val imagesURL = java.util.ArrayList<String>()

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

        val userNameEditText: EditText = findViewById(R.id.editTextValorantName)
        val findaccountButton: Button = findViewById(R.id.generalStats)
        val MMR: Button = findViewById(R.id.MMR)
        val matchHistoryButton: Button = findViewById(R.id.matchHistory)
        val updatesButton: Button = findViewById(R.id.updateBT)
        val compareButton: Button = findViewById(R.id.compareBT)
        val local: Button = findViewById(R.id.download)
        val viewMatch: Button = findViewById(R.id.viewHistory)
        val leaderBoard: Button = findViewById(R.id.leaderboard)
        val recentPlayers: Button = findViewById(R.id.recentPlayers)
        val addname: Button = findViewById(R.id.addname)
        val delete: Button = findViewById(R.id.delete)
        val mySpinner = findViewById<View>(R.id.spinner) as Spinner

        val strings = java.util.ArrayList<String>()
        val file = File(this.filesDir, "texts")
        if (!file.exists()) {
            file.mkdir()
        }
        val gpxfile = File(file, "users")
        if (!gpxfile.exists()) {
            gpxfile.createNewFile()
        }
        gpxfile.forEachLine {
            strings.add(it)
        }

        val arrayAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, strings)
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mySpinner.adapter = arrayAdapter

        imagebackground = findViewById(R.id.imagebackground)
        Picasso.get().load(imagesURL.random()).into(imagebackground)
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            doTask(handler)
        }

        val matchDatabse = MatchDatabases(this@FindAccount)
        syncFireBase()

        delete.setOnClickListener {
            val file = File(this.filesDir, "texts")
            if (file.exists()) {
                val gpxfile = File(file, "users")
                if (gpxfile.exists()) {
                    if (gpxfile.readText() == "") {
                        Toast.makeText(this, "Already Deleted!", Toast.LENGTH_SHORT).show()
                    } else {
                        gpxfile.delete()
                        Toast.makeText(this, "Deleted!", Toast.LENGTH_SHORT).show()
                        refresh()
                    }
                }
            }
        }

        addname.setOnClickListener {
            if (!isEmpty()) {
                if (verify(userNameEditText.text.toString())) {
                    val file = File(this.filesDir, "texts")
                    if (!file.exists()) {
                        file.mkdir()
                    }
                    try {
                        val gpxfile = File(file, "users")
                        if (!gpxfile.exists()) {
                            gpxfile.createNewFile()
                        }
                        var pass = true
                        gpxfile.forEachLine {
                            if (it == userNameEditText.text.toString()) {
                                val contextView = findViewById<View>(R.id.generalStats)
                                val snackbar = Snackbar
                                    .make(
                                        contextView,
                                        "User already in file!",
                                        Snackbar.LENGTH_LONG
                                    )
                                snackbar.show()
                                pass = false
                            }
                        }

                        if (pass) {
                            if (gpxfile.readText() == "") {
                                gpxfile.appendText(userNameEditText.text.toString())
                            } else {
                                gpxfile.appendText("\n" + userNameEditText.text.toString())
                            }
                            Toast.makeText(this, "Saved!", Toast.LENGTH_SHORT).show()
                            refresh()
                        }
                    } catch (e: java.lang.Exception) {
                        Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }

        }


        handler.postDelayed(runnable, 2000)

        matchHistoryButton.setOnClickListener {
            if (mySpinner.selectedItem != null) {
                findMatches(mySpinner.selectedItem.toString())
            }
        }

        findaccountButton.setOnClickListener {
            if (mySpinner.selectedItem != null) {
                findAccount(mySpinner.selectedItem.toString())
            }
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
            val fullname = mySpinner.selectedItem.toString()
            val name = fullname.split("#")
            val doesUserExist =
                "https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true"
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Verifying User")
            progressDialog.setMessage("Checking if user exists.")
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setCancelable(false)
            progressDialog.show()

            doAsync {
                try {
                    JSONObject(URL(doesUserExist).readText())
                    val intent = Intent(this@FindAccount, MMRActivity::class.java)
                    intent.putExtra("RiotName", name[0])
                    intent.putExtra("RiotID", name[1])
                    progressDialog.dismiss()
                    startActivity(intent)
                } catch (e: Exception) {
                    progressDialog.dismiss()
                    uiThread {
                        val contextView = findViewById<View>(R.id.generalStats)
                        val snackbar = Snackbar
                            .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
                        snackbar.show()
                    }
                }
            }


        }

        leaderBoard.setOnClickListener {
            startActivity(Intent(this, leaderBoardActivity::class.java))
        }


        viewMatch.setOnClickListener {
            val intent = Intent(this@FindAccount, ViewMatches::class.java)
            intent.putExtra("RiotName", mySpinner.selectedItem.toString())
            startActivity(intent)
        }

        local.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            progressDialog.setTitle("Saving Matches")
            progressDialog.setMessage("Storing matches on local storage.")
            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
            progressDialog.setCancelable(false)
            progressDialog.show()

            //Get matches
            val fullname = mySpinner.selectedItem.toString()
            val name = fullname.split("#")
            val matchHistoryURL =
                "https://api.henrikdev.xyz/valorant/v3/matches/eu/${name[0]}/${name[1]}?size=10"
            doAsync {
                try {
                    val matchhistoryURL = URL(matchHistoryURL).readText()
                    val jsonMatches = JSONObject(matchhistoryURL)
                    val data = jsonMatches["data"] as JSONArray

                    for (i in 0 until data.length()) {
                        val map = data.getJSONObject(i).getJSONObject("metadata").getString("map")
                        val mode = data.getJSONObject(i).getJSONObject("metadata").getString("mode")
                        val matchID =
                            data.getJSONObject(i).getJSONObject("metadata").getString("matchid")
                        val matchDatabse = MatchDatabases(this@FindAccount)
                        val database = Firebase.database
                        val playersRef = database.getReference("VALORANT/players")
                        playersRef.child(name[0]).child("Tag").setValue(name[1])
                        playersRef.child(name[0]).child("Matches").child(matchID).child("Map")
                            .setValue(map)
                        playersRef.child(name[0]).child("Matches").child(matchID).child("Mode")
                            .setValue(mode)
                        matchDatabse.addMatches(
                            matchID,
                            mySpinner.selectedItem.toString(),
                            map,
                            mode
                        )
                    }

                    //Save data
                    uiThread {
                        val matchDatabse = MatchDatabases(this@FindAccount)
                        val matches = matchDatabse.getMatches((mySpinner.selectedItem.toString()))
                        progressDialog.dismiss()
                        if (matches != null) {
                            Toast.makeText(
                                this@FindAccount,
                                "${matches.size} matches saved for ${mySpinner.selectedItem}!",
                                Toast.LENGTH_SHORT
                            ).show()
                            syncFireBase()
                        } else {
                            Toast.makeText(
                                this@FindAccount,
                                "No saved matches for this user!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: java.io.FileNotFoundException) {
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

        recentPlayers.setOnClickListener {
            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "Searching for players...", Snackbar.LENGTH_LONG)
            snackbar.show()
            val fullName = mySpinner.selectedItem.toString()
            val split = fullName.split("#")
            val matchHistoryURL =
                "https://api.henrikdev.xyz/valorant/v3/matches/eu/${split[0]}/${split[1]}?size=10"
            doAsync {
                try {
                    val json = JSONObject(URL(matchHistoryURL).readText())
                    val data = json["data"] as JSONArray
                    val players: ArrayList<String> = ArrayList()
                    var filterMode = arrayOf("")
                    for (i in 0 until data.length()) {
                        val allPlayers = data.getJSONObject(i).getJSONObject("players")
                            .getJSONArray("all_players") as JSONArray
                        for (j in 0 until allPlayers.length()) {
                            players += allPlayers.getJSONObject(j)
                                .getString("name") + "#" + allPlayers.getJSONObject(j)
                                .getString("tag")
                        }
                    }
                    for (player in players) {
                        if (!filterMode.contains(player)) {
                            filterMode += player
                        }
                    }
                    var i = 0
                    var finalName: String? = null
                    for (player in filterMode) {
                        val occurrences = Collections.frequency(players, player)
                        if (occurrences > i) {
                            if (fullName != player) {
                                i = occurrences
                                if (i != 1) {
                                    finalName = player
                                }
                            }
                        }
                    }
                    if (finalName != null) {
                        uiThread {
                            val builder = AlertDialog.Builder(this@FindAccount)
                            builder.setTitle("$finalName was in $i/10 games")
                            builder.setItems(
                                arrayOf<CharSequence>(
                                    "Coming soon!"
                                )
                            )
                            { _, which ->
                                when (which) {
                                    //0 ->
                                }
                            }
                            val dialog = builder.create()
                            dialog.window!!.attributes.windowAnimations =
                                R.style.DialogAnimation_2
                            dialog.show()
                        }
                    } else {
                        val snackbar = Snackbar
                            .make(contextView, "No matching players found!", Snackbar.LENGTH_SHORT)
                        snackbar.show()
                    }
                } catch (e: FileNotFoundException) {
                    val snackbar = Snackbar
                        .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
                    snackbar.show()
                } catch (e: Exception) {
                    Log.d("players", e.toString())
                    val snackbar = Snackbar
                        .make(contextView, "Error occurred!", Snackbar.LENGTH_LONG)
                    snackbar.show()

                }
            }
        }
    }

    private fun doTask(handler: Handler){
        Picasso.get().load(imagesURL.random()).placeholder(imagebackground.drawable)
            .into(imagebackground)
        handler.postDelayed(runnable, 5000)
    }

    private fun isEmpty(): Boolean {
        val userNameEditText : EditText = findViewById(R.id.editTextValorantName)
        if (userNameEditText.text.isNotEmpty()){
            return false
        }
        else {
            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "Enter something!", Snackbar.LENGTH_LONG)
            snackbar.show()
        }
        return true
    }

    private fun verify(RiotName: String) : Boolean {
        val name = RiotName
        val check: Boolean = "#" in name

        if (check){
            try {
                val imm: InputMethodManager =
                    getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
            } catch (e: java.lang.Exception) {
            }
            return true
        }
        else {
            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "Include the # in the Riot Name!", Snackbar.LENGTH_LONG)
            snackbar.show()
        }
        return false
    }

    private fun isNetworkAvailable(): Boolean { val cm = getSystemService(
        Context.CONNECTIVITY_SERVICE
    ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }


    private fun findAccount(RiotName: String) {

        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Fetching Stats")
        progressDialog.setMessage("Please wait a moment")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
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
            val doesUserExist = "https://api.henrikdev.xyz/valorant/v1/account/$Name/$ID?force=true"
            val URLtoFindAccount = "https://api.tracker.gg/api/v2/valorant/standard/profile/riot/$Name%23$ID"

            doAsync {
                try {
                    try
                    {
                        var userExist = false
                        try {
                            val existUserText = URL(doesUserExist).readText()
                            val timetoFindOut = JSONObject(existUserText)
                            val datatoseeiftheyexist = timetoFindOut["data"] as JSONObject
                            datatoseeiftheyexist["name"].toString()
                            userExist = true
                            val database = Firebase.database
                            val playersRef = database.getReference("VALORANT/players")
                            playersRef.child(Name).child("Tag").setValue(ID)
                        }
                        catch (e: java.io.FileNotFoundException){
                            progressDialog.dismiss()
                            runOnUiThread {
                                val contextView = findViewById<View>(R.id.generalStats)
                                val snackbar = Snackbar
                                    .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
                                snackbar.show()
                            }
                        }

                        if (userExist) {
                            val text = URL(URLtoFindAccount).readText()
                            val answer = JSONObject(text)
                            val data = answer["data"] as JSONObject
                            val platFormInfo = data["platformInfo"] as JSONObject
                            PLAYERCARD = platFormInfo["avatarUrl"].toString()
                            runOnUiThread {
                                val intent = Intent(this@FindAccount, StatsActivity::class.java)
                                intent.putExtra("RiotName", Name)
                                intent.putExtra("RiotID", ID)
                                intent.putExtra("URL", PLAYERCARD)
                                progressDialog.dismiss()
                                startActivity(intent)
                            }
                        }

                    } catch (e: java.io.FileNotFoundException) {

                        val SignInUrl = "https://tracker.gg/valorant/profile/riot/$Name%23$ID/overview"

                        runOnUiThread {
                            progressDialog.dismiss()
                            val d =
                                AlertDialog.Builder(this@FindAccount).setTitle("Private account!")
                                    .setMessage(
                                        Html.fromHtml(
                                            "It appears this account '$RiotName' is private.\nTo access this data the owner of the account will need to sign in with" +
                                                    "<a href=\"$SignInUrl\"> this link</a>"
                                        )
                                    )
                                .setPositiveButton(android.R.string.ok) { _, _ -> }
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                            (d.findViewById<TextView>(android.R.id.message)!!).movementMethod =
                                LinkMovementMethod.getInstance()
                            }
                        }
                    }

                    catch (e: java.io.FileNotFoundException) {
                        progressDialog.dismiss()
                        val contextView = findViewById<View>(R.id.generalStats)
                        val snackbar = Snackbar
                            .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
                        snackbar.show()
                    }


                catch (e: Exception) {
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
            val doesUserExist = "https://api.henrikdev.xyz/valorant/v1/account/$Name/$ID"
            val matchHistoryURL =
                "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID?size=10"

            doAsync {
                try{
                    val existUserText = URL(doesUserExist).readText()
                    val timetoFindOut = JSONObject(existUserText)
                    val datatoseeiftheyexist = timetoFindOut["data"] as JSONObject
                    datatoseeiftheyexist["name"].toString()
                    //find Matches
                    try{
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

                    }
                    catch (e: java.io.FileNotFoundException) {
                        uiThread {
                            progressDialog.dismiss()
                            AlertDialog.Builder(this@FindAccount).setTitle("Server Error!")
                                .setMessage("This seems to be a server error which will be fixed soon! (hopefully)\"\nError Message:\n$e")
                                .setPositiveButton(android.R.string.ok) { _, _ -> }
                                .setIcon(android.R.drawable.ic_dialog_alert).show()
                        }
                    }
                    catch (e: Exception) {
                        uiThread {
                            progressDialog.dismiss()
                            AlertDialog.Builder(this@FindAccount).setTitle("Error!")
                                .setMessage("Error Message: $e")
                                .setPositiveButton(android.R.string.ok) { _, _ -> }
                                .setIcon(android.R.drawable.ic_dialog_alert).show()
                        }
                    }


                }
                catch (e: java.io.FileNotFoundException){
                    uiThread{
                        progressDialog.dismiss()
                        val contextView = findViewById<View>(R.id.generalStats)
                        val snackbar = Snackbar
                            .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
                        snackbar.show()
                    }
                }
                catch (e: Exception) {
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
        val i = Intent(this, FindAccount::class.java)
        startActivity(i)
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

            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "Database synced!", Snackbar.LENGTH_SHORT)
            snackbar.show()

        } catch (e: Exception) {
            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "Database not synced!", Snackbar.LENGTH_SHORT)
            snackbar.show()
        }
    }

}
