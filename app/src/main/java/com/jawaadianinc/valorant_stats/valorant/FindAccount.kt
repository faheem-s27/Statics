package com.jawaadianinc.valorant_stats.valorant

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
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
import java.util.*

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
        //val local: Button = findViewById(R.id.download)
        val viewMatch: Button = findViewById(R.id.viewHistory)
        val leaderBoard: Button = findViewById(R.id.leaderboard)
        val recentPlayers: Button = findViewById(R.id.recentPlayers)
        val addname: Button = findViewById(R.id.addname)
        val mySpinner = findViewById<View>(R.id.spinner) as Spinner
        val refreshPlayer: ImageButton = findViewById(R.id.refreshUpdate)

        val strings = java.util.ArrayList<String>()
        val file = File(this.filesDir, "texts")
        if (!file.exists()) {
            file.mkdir()
        }
        val gpxfile = File(file, "Players")
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

        mySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                updatePlayerUI()
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
            }
        }

        imagebackground = findViewById(R.id.imagebackground)
        Picasso.get().load(imagesURL.random()).into(imagebackground)
//        handler = Handler(Looper.getMainLooper())
//        runnable = Runnable {
//            doTask(handler)
//        }
        syncFireBase()
        addname.setOnClickListener {
            if (!isEmpty()) {
                if (verify(userNameEditText.text.toString())) {
                    val progressDialog = ProgressDialog(this)
                    progressDialog.setTitle("Verifying User")
                    progressDialog.setMessage("Checking if user exists.")
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                    progressDialog.setCancelable(false)
                    progressDialog.show()

                    val file = File(this.filesDir, "texts")
                    if (!file.exists()) {
                        file.mkdir()
                    }
                    try {
                        val gpxfile = File(file, "Players")
                        if (!gpxfile.exists()) {
                            gpxfile.createNewFile()
                        }
                        var pass = true
                        gpxfile.forEachLine {
                            if (it == userNameEditText.text.toString()) {
                                val contextView = findViewById<View>(R.id.playerLevel)
                                val snackbar = Snackbar
                                    .make(
                                        contextView,
                                        "User already exists!",
                                        Snackbar.LENGTH_LONG
                                    )
                                snackbar.show()
                                pass = false
                                progressDialog.dismiss()
                            }
                        }
                        if (pass) {
                            //Name Verification
                            doAsync {
                                try {
                                    if (gpxfile.readText() == "") {
                                        gpxfile.appendText(userNameEditText.text.toString())
                                    } else {
                                        gpxfile.appendText("\n" + userNameEditText.text.toString())
                                    }
                                    val name = userNameEditText.text.toString().split("#")
                                    val data =
                                        JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true").readText())["data"] as JSONObject
                                    val database = Firebase.database
                                    val playersRef = database.getReference("VALORANT/players")
                                    playersRef.child(name[0]).child("Tag").setValue(name[1])
                                    playersRef.child(name[0]).child("Avatar")
                                        .setValue(data.getJSONObject("card").get("small"))
                                    playersRef.child(name[0]).child("AvatarLarge")
                                        .setValue(data.getJSONObject("card").get("large"))
                                    playersRef.child(name[0]).child("Level")
                                        .setValue(data.get("account_level"))
                                    uiThread {
                                        progressDialog.dismiss()
                                        Toast.makeText(
                                            this@FindAccount,
                                            "Saved!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        refresh()
                                    }

                                } catch (e: Exception) {
                                    uiThread {
                                        progressDialog.dismiss()
                                        val contextView = findViewById<View>(R.id.playerLevel)
                                        val snackbar = Snackbar
                                            .make(
                                                contextView,
                                                "User not found!",
                                                Snackbar.LENGTH_LONG
                                            )
                                        snackbar.show()
                                    }
                                }
                            }
                        }
                    } catch (e: java.lang.Exception) {
                        progressDialog.dismiss()
                        Toast.makeText(this, "Error: $e", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        refreshPlayer.setOnClickListener {
            val name = mySpinner.selectedItem.toString().split("#")
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
                updatePlayerUI()
                val contextView = findViewById<View>(R.id.playerLevel)
                uiThread {
                    val snackbar = Snackbar
                        .make(contextView, "Player data updated!", Snackbar.LENGTH_LONG)
                    snackbar.show()
                }
            }
        }

        //handler.postDelayed(runnable, 2000)
        updatePlayerUI()

        matchHistoryButton.setOnClickListener {
            if (mySpinner.selectedItem != null) {
                findMatches(mySpinner.selectedItem.toString())
            }
        }

        findaccountButton.setOnClickListener {
//            if (mySpinner.selectedItem != null) {
//                findAccount(mySpinner.selectedItem.toString())
//            }
            //TODO add this after fix
//            AlertDialog.Builder(this).setTitle("New feature!")
//                .setMessage("Due to legal issues, I cannot display all previous data about a user, however I am working on a new overall stats screen" +
//                        " based on the last 10 matches. (In development)")
//                .setPositiveButton(android.R.string.ok) { _, _ ->
//                    //startActivity(Intent(this, valorantPlayerStatsOverall::class.java))
//                    Toast.makeText(applicationContext,
//                        "Well done", Toast.LENGTH_SHORT).show()
//                }
//                .setNegativeButton("No"){ _, _ -> }
//                .setIcon(android.R.drawable.ic_dialog_alert).show()
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
            val intent = Intent(this@FindAccount, MMRActivity::class.java)
            intent.putExtra("RiotName", name[0])
            intent.putExtra("RiotID", name[1])
            startActivity(intent)
        }

        leaderBoard.setOnClickListener {
            startActivity(Intent(this, leaderBoardActivity::class.java))
        }

        viewMatch.setOnClickListener {
            val intent = Intent(this@FindAccount, ViewMatches::class.java)
            intent.putExtra("RiotName", mySpinner.selectedItem.toString())
            startActivity(intent)
        }

//        local.setOnClickListener {
//            val progressDialog = ProgressDialog(this)
//            progressDialog.setTitle("Saving Matches")
//            progressDialog.setMessage("Storing matches on local storage.")
//            progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
//            progressDialog.setCancelable(false)
//            progressDialog.show()
//
//            //Get matches
//            val fullname = mySpinner.selectedItem.toString()
//            val name = fullname.split("#")
//            val matchHistoryURL =
//                "https://api.henrikdev.xyz/valorant/v3/matches/eu/${name[0]}/${name[1]}?size=10"
//            doAsync {
//                try {
//                    val matchhistoryURL = URL(matchHistoryURL).readText()
//                    val jsonMatches = JSONObject(matchhistoryURL)
//                    val data = jsonMatches["data"] as JSONArray
//
//                    for (i in 0 until data.length()) {
//                        val map = data.getJSONObject(i).getJSONObject("metadata").getString("map")
//                        val mode = data.getJSONObject(i).getJSONObject("metadata").getString("mode")
//                        val matchID =
//                            data.getJSONObject(i).getJSONObject("metadata").getString("matchid")
//                        val matchDatabse = MatchDatabases(this@FindAccount)
//                        val database = Firebase.database
//                        val playersRef = database.getReference("VALORANT/players")
//                        playersRef.child(name[0]).child("Tag").setValue(name[1])
//                        playersRef.child(name[0]).child("Matches").child(matchID).child("Map")
//                            .setValue(map)
//                        playersRef.child(name[0]).child("Matches").child(matchID).child("Mode")
//                            .setValue(mode)
//                        matchDatabse.addMatches(
//                            matchID,
//                            mySpinner.selectedItem.toString(),
//                            map,
//                            mode
//                        )
//                    }
//
//                    //Save data
//                    uiThread {
//                        val matchDatabse = MatchDatabases(this@FindAccount)
//                        val matches = matchDatabse.getMatches((mySpinner.selectedItem.toString()))
//                        progressDialog.dismiss()
//                        if (matches != null) {
//                            Toast.makeText(
//                                this@FindAccount,
//                                "${matches.size} matches saved for ${mySpinner.selectedItem}!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                            syncFireBase()
//                        } else {
//                            Toast.makeText(
//                                this@FindAccount,
//                                "No saved matches for this user!",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                } catch (e: FileNotFoundException) {
//                    uiThread {
//                        progressDialog.dismiss()
//                        val contextView = findViewById<View>(R.id.generalStats)
//                        val snackbar = Snackbar
//                            .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
//                        snackbar.show()
//                    }
//                } catch (e: Exception) {
//                    uiThread {
//                        progressDialog.dismiss()
//                        AlertDialog.Builder(this@FindAccount).setTitle("Error!")
//                            .setMessage("Error Message: $e")
//                            .setPositiveButton(android.R.string.ok) { _, _ -> }
//                            .setIcon(android.R.drawable.ic_dialog_alert).show()
//                    }
//                }
//            }
//        }

        recentPlayers.setOnClickListener {
            val contextView = findViewById<View>(R.id.playerLevel)
            val snackbar = Snackbar
                .make(contextView, "Looking for recent players. Wait...", Snackbar.LENGTH_LONG)
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
                    val mapofPlayerOccurences: MutableMap<String, Int> = mutableMapOf("Name" to 10)
                    val finalName: String? = null
                    for (player in filterMode) {
                        val occurrences = Collections.frequency(players, player)
                        if (player != fullName) {
                            if (occurrences > 1) {
                                mapofPlayerOccurences[player] = occurrences
                            }
                        }
                    }

                    mapofPlayerOccurences.remove("Name", 10)
                    val result =
                        mapofPlayerOccurences.toList().sortedBy { (_, value) -> value }.toMap()
                    var finalList = arrayOf("")
                    val list = result.toList()
                    list.forEach {
                        finalList += it.first + " in ${it.second} games"
                    }
                    finalList.reverse()
                    finalList = finalList.filter { x: String? -> x != "" }.toTypedArray()
                    if (finalList.isNotEmpty()) {
                        uiThread {
                            val builder = AlertDialog.Builder(this@FindAccount)
                            builder.setTitle("Found ${filterMode.count()} players in last 10 games!")
                            builder.setItems(finalList,
                                DialogInterface.OnClickListener { _, itemIndex ->
                                    addNameToList(finalList[itemIndex])
                                })
                            val dialog = builder.create()
                            dialog.window!!.attributes.windowAnimations =
                                R.style.DialogAnimation_2
                            dialog.show()
                        }
                    } else {
                        val snackbar = Snackbar
                            .make(contextView, "No repeating players found!", Snackbar.LENGTH_SHORT)
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

    private fun updatePlayerUI() {
        val mySpinner = findViewById<View>(R.id.spinner) as Spinner
        val playerProfile: ImageView = findViewById(R.id.playerProfile)
        val playerLevel: TextView = findViewById(R.id.playerLevel)
        val playerName: TextView = findViewById(R.id.playerNameMenu)
        doAsync {
            try {
                val fullname = mySpinner.selectedItem.toString()
                if (fullname.isNotBlank()) {
                    val name = fullname.split("#")
                    uiThread {
                        val database = Firebase.database
                        val playersRef = database.getReference("VALORANT/players")
                        playersRef.child(name[0]).child("Avatar")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        val avatar = dataSnapshot.value as String
                                        Picasso.get().load(avatar).fit().centerInside()
                                            .into(playerProfile)
                                    } catch (e: Exception) {
                                        doAsync {
                                            val data =
                                                JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true").readText())["data"] as JSONObject
                                            val database = Firebase.database
                                            val playersRef =
                                                database.getReference("VALORANT/players")
                                            playersRef.child(name[0]).child("Tag").setValue(name[1])
                                            playersRef.child(name[0]).child("Avatar")
                                                .setValue(data.getJSONObject("card").get("small"))
                                            playersRef.child(name[0]).child("AvatarLarge")
                                                .setValue(data.getJSONObject("card").get("large"))
                                            playersRef.child(name[0]).child("Level")
                                                .setValue(data.get("account_level"))
                                            uiThread {
                                                updatePlayerUI()
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            })
                        playersRef.child(name[0]).child("Level")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        playerLevel.text = (dataSnapshot.value as Long).toString()
                                    } catch (e: Exception) {
                                        doAsync {
                                            val data =
                                                JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true").readText())["data"] as JSONObject
                                            val database = Firebase.database
                                            val playersRef =
                                                database.getReference("VALORANT/players")
                                            playersRef.child(name[0]).child("Tag").setValue(name[1])
                                            playersRef.child(name[0]).child("Avatar")
                                                .setValue(data.getJSONObject("card").get("small"))
                                            playersRef.child(name[0]).child("AvatarLarge")
                                                .setValue(data.getJSONObject("card").get("large"))
                                            playersRef.child(name[0]).child("Level")
                                                .setValue(data.get("account_level"))
                                            uiThread {
                                                updatePlayerUI()
                                            }
                                        }
                                    }
                                }
                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            })
                        playersRef.child(name[0]).child("AvatarLarge")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    try {
                                        Picasso.get().load(dataSnapshot.value.toString())
                                            .into(imagebackground)
                                    } catch (e: Exception) {
                                        doAsync {
                                            val data =
                                                JSONObject(URL("https://api.henrikdev.xyz/valorant/v1/account/${name[0]}/${name[1]}?force=true").readText())["data"] as JSONObject
                                            val database = Firebase.database
                                            val playersRef =
                                                database.getReference("VALORANT/players")
                                            playersRef.child(name[0]).child("Tag").setValue(name[1])
                                            playersRef.child(name[0]).child("Avatar")
                                                .setValue(data.getJSONObject("card").get("small"))
                                            playersRef.child(name[0]).child("AvatarLarge")
                                                .setValue(data.getJSONObject("card").get("large"))
                                            playersRef.child(name[0]).child("Level")
                                                .setValue(data.get("account_level"))
                                            uiThread {
                                                updatePlayerUI()
                                            }
                                        }
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                }
                            })

                        playerName.text = mySpinner.selectedItem.toString()
                    }
                }
            } catch (e: Exception) {
                Log.d("test", e.toString())
            }
        }
    }

    private fun doTask(handler: Handler) {
//        Picasso.get().load(imagesURL.random()).placeholder(imagebackground.drawable)
//            .into(imagebackground)
//        handler.postDelayed(runnable, 5000)
    }

    private fun isEmpty(): Boolean {
        val userNameEditText: EditText = findViewById(R.id.editTextValorantName)
        if (userNameEditText.text.isNotEmpty()) {
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
                            val timetoFindOut = JSONObject(URL(doesUserExist).readText())
                            val datatoseeiftheyexist = timetoFindOut["data"] as JSONObject
                            val playerName = datatoseeiftheyexist["name"].toString()
                            Log.d("test", playerName)
                            val database = Firebase.database
                            val playersRef = database.getReference("VALORANT/players")
                            playersRef.child(Name).child("Tag").setValue(ID)

                            val data = datatoseeiftheyexist["card"] as JSONObject
                            val platFormInfo = data["small"] as String
                            PLAYERCARD = platFormInfo

                            runOnUiThread {
                                val intent = Intent(this@FindAccount, StatsActivity::class.java)
                                intent.putExtra("RiotName", Name)
                                intent.putExtra("RiotID", ID)
                                intent.putExtra("URL", PLAYERCARD)
                                progressDialog.dismiss()
                                startActivity(intent)
                            }

                        } catch (e: FileNotFoundException) {
                            progressDialog.dismiss()
                            runOnUiThread {
                                val contextView = findViewById<View>(R.id.generalStats)
                                val snackbar = Snackbar
                                    .make(contextView, "User not found!", Snackbar.LENGTH_LONG)
                                snackbar.show()
                            }
                        }
                    } catch (e: FileNotFoundException) {
                        val SignInUrl =
                            "https://tracker.gg/valorant/profile/riot/$Name%23$ID/overview"
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
                    } catch (e: FileNotFoundException) {
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
            val matchHistoryURL =
                "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID?size=10"

            doAsync {
                try {
//                    val existUserText = URL(doesUserExist).readText()
//                    val timetoFindOut = JSONObject(existUserText)
//                    val datatoseeiftheyexist = timetoFindOut["data"] as JSONObject
//                    datatoseeiftheyexist["name"].toString()
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

            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "Welcome back!", Snackbar.LENGTH_SHORT)
            snackbar.show()

        } catch (e: Exception) {
            val contextView = findViewById<View>(R.id.generalStats)
            val snackbar = Snackbar
                .make(contextView, "Database not synced!", Snackbar.LENGTH_SHORT)
            snackbar.show()
            Log.d("database", e.toString())
        }
    }
}
