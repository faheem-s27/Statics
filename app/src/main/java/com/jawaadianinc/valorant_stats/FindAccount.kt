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
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.net.URL


class FindAccount : AppCompatActivity() {

    var PLAYERCARD = ""
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var imagebackground :ImageView

    val imagesURL = arrayOf(
        "https://mfiles.alphacoders.com/918/918510.jpg",
        "https://mfiles.alphacoders.com/921/921074.jpg",
        "https://i.pinimg.com/originals/3a/1f/70/3a1f70e6b2fad80fa59a34dab44b7882.gif",
        "https://mfiles.alphacoders.com/908/908217.png",
        "https://mfiles.alphacoders.com/864/864178.png",
        "https://1.bp.blogspot.com/-w9Si9jtaaK4/XuFPpqPZqqI/AAAAAAAASWY/GCrORIscy-8zubmSvrJ2_6qkmt3lXMP4QCK4BGAsYHg/d/valorant-wallpaper-2020-06-10-172425-2-heroscreen.cc.jpg",
        "https://hdandroidwallpaper.com/wp-content/uploads/2020/12/valorant-Wallpaper-Mobile.jpg",
        "https://www.mordeo.org/files/uploads/2020/05/Sage-Valorant-4K-Ultra-HD-Mobile-Wallpaper-950x1689.jpg"
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaccount)

        val userNameEditText: EditText = findViewById(R.id.editTextValorantName)
        val findaccountButton: Button = findViewById(R.id.generalStats)
        val matchHistoryButton: Button = findViewById(R.id.matchHistory)
        val updatesButton: Button = findViewById(R.id.updateBT)
        val compareButton: Button = findViewById(R.id.compareBT)

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
                                AlertDialog.Builder(this).setTitle("Name is already in file!")
                                    .setMessage("Please enter something else and try again!")
                                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                                    .setIcon(android.R.drawable.ic_dialog_alert).show()
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


        handler.postDelayed(runnable, 500)

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


    }

    private fun doTask(handler: Handler){
        Picasso.get().load(imagesURL.random()).placeholder(imagebackground.drawable).into(imagebackground)
        handler.postDelayed(runnable,4000)

    }

    private fun isEmpty(): Boolean {
        val userNameEditText : EditText = findViewById(R.id.editTextValorantName)
        if (userNameEditText.text.isNotEmpty()){
            return false
        }
        else{
            AlertDialog.Builder(this).setTitle("Nothing there!")
                .setMessage("Please enter something in the field and try again!")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
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
        else{
            AlertDialog.Builder(this).setTitle("Incorrect Format")
                .setMessage("Please ensure the name includes the #")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
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
                        }
                        catch (e: java.io.FileNotFoundException){
                            progressDialog.dismiss()
                            runOnUiThread {
                                AlertDialog.Builder(this@FindAccount)
                                    .setTitle("User Not Found!")
                                    .setMessage("It appears this name '$RiotName' does not exist!\nTry a different name")
                                    .setPositiveButton(android.R.string.ok) { _, _ -> }
                                    .setIcon(android.R.drawable.ic_dialog_alert)
                                    .show()
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
                            val d = AlertDialog.Builder(this@FindAccount).setTitle("Private account!")
                                .setMessage(Html.fromHtml("It appears this name '$RiotName' is private.\nTo access that data they will need to sign in with" +
                                        "<a href=\"$SignInUrl\"> this link</a>\nHowever they can still check their match history!"))
                                .setPositiveButton(android.R.string.ok) { _, _ -> }
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show()
                            (d.findViewById<TextView>(android.R.id.message)!!).movementMethod =
                                LinkMovementMethod.getInstance()
                            }
                        }
                    }

                    catch (e: java.io.FileNotFoundException){
                        progressDialog.dismiss()
                        AlertDialog.Builder(this@FindAccount).setTitle("User Not Found!")
                            .setMessage("It appears this name '$RiotName' does not exist!\nTry a different name")
                            .setPositiveButton(android.R.string.ok) { _, _ -> }
                            .setIcon(android.R.drawable.ic_dialog_alert)
                            .show()
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
        else{
            AlertDialog.Builder(this).setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
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

                            for (i in 0 until data.length()){
                                val map = data.getJSONObject(i).getJSONObject("metadata").getString("map")
                                val mode = data.getJSONObject(i).getJSONObject("metadata").getString("mode")
                                matches.add("\n$mode on $map")
                            }

                            uiThread {
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
                                        0 -> matchActivityStart(Name, ID, 0)
                                        1 -> matchActivityStart(Name, ID, 1)
                                        2 -> matchActivityStart(Name, ID, 2)
                                        3 -> matchActivityStart(Name, ID, 3)
                                        4 -> matchActivityStart(Name, ID, 4)
                                        5 -> matchActivityStart(Name, ID, 5)
                                        6 -> matchActivityStart(Name, ID, 6)
                                        7 -> matchActivityStart(Name, ID, 7)
                                        8 -> matchActivityStart(Name, ID, 8)
                                        9 -> matchActivityStart(Name, ID, 9)
                                    }
                                }
                                progressDialog.dismiss()
                                builder.create().show()
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
                    AlertDialog.Builder(this@FindAccount).setTitle("User Not Found!")
                        .setMessage("It appears this name '$RiotName' does not exist!\nTry a different name")
                        .setPositiveButton(android.R.string.ok) { _, _ -> }
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
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
        else{
            AlertDialog.Builder(this).setTitle("No Internet Connection")
                .setMessage("Please check your internet connection and try again")
                .setPositiveButton(android.R.string.ok) { _, _ -> }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }

    }

    private fun matchActivityStart(Name: String, ID:String, matchNumber: Int){
        val matchintent = Intent(this@FindAccount, MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", matchNumber)
        startActivity(matchintent)

    }

    private fun compareStats() {
        startActivity(Intent(this, CompareActivity::class.java))
    }


    fun refresh() {
        finish()
        val i = Intent(this, FindAccount::class.java)
        startActivity(i)
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
    }


}