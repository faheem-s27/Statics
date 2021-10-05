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
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


class FindAccount : AppCompatActivity() {

    var PLAYERCARD = ""
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var imagebackground :ImageView

    val imagesURL = arrayOf("https://mfiles.alphacoders.com/918/918510.jpg",
        "https://mfiles.alphacoders.com/929/929666.jpg",
        "https://mfiles.alphacoders.com/878/878029.jpg",
        "https://mfiles.alphacoders.com/929/929924.png",
        "https://mfiles.alphacoders.com/921/921074.jpg",
        "https://mfiles.alphacoders.com/908/908217.png",
        "https://mfiles.alphacoders.com/864/864178.png",
    "https://1.bp.blogspot.com/-w9Si9jtaaK4/XuFPpqPZqqI/AAAAAAAASWY/GCrORIscy-8zubmSvrJ2_6qkmt3lXMP4QCK4BGAsYHg/d/valorant-wallpaper-2020-06-10-172425-2-heroscreen.cc.jpg",
    "https://hdandroidwallpaper.com/wp-content/uploads/2020/12/valorant-Wallpaper-Mobile.jpg",
    "https://www.mordeo.org/files/uploads/2020/05/Sage-Valorant-4K-Ultra-HD-Mobile-Wallpaper-950x1689.jpg")


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_findaccount)

        val userNameEditText : EditText = findViewById(R.id.editTextValorantName)
        val findaccountButton : Button = findViewById(R.id.generalStats)
        val matchHistoryButton : Button = findViewById(R.id.matchHistory)
        val agentsButton : Button = findViewById(R.id.agents)
        val updatesButton : Button = findViewById(R.id.updateBT)

        imagebackground = findViewById(R.id.imagebackground)
        Picasso.get().load(imagesURL.random()).into(imagebackground)

        handler = Handler(Looper.getMainLooper())

        runnable = Runnable {
            doTask(handler)
        }

        handler.postDelayed(runnable, 500)

        matchHistoryButton.setOnClickListener{
            if (!isEmpty()){
                if (verify(userNameEditText.text.toString())){
                    findMatches(userNameEditText.text.toString())
                }
            }
        }

        findaccountButton.setOnClickListener{
            if (!isEmpty()){
                if (verify(userNameEditText.text.toString())){
                    findAccount(userNameEditText.text.toString())
                }
            }
        }

        agentsButton.setOnClickListener{
            if (!isEmpty()){
                if (verify(userNameEditText.text.toString())){
                    val agentsIntent = Intent(this@FindAccount, AgentsActivity::class.java)
                    var name = ""
                    name = when (userNameEditText.text.toString()) {
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
                            userNameEditText.text.toString()
                        }
                    }
                    agentsIntent.putExtra("Name", name)
                    startActivity(agentsIntent)
                }
            }
        }
        
        updatesButton.setOnClickListener{
            startActivity(Intent(this, ValorantUpdatesActivity::class.java))
        }

        //COMPARE STUFF WOOP WOOP
        findViewById<Button>(R.id.compareButton).setOnClickListener{
            if (!isEmpty()){
                if (verify(userNameEditText.text.toString())){
                    var name = ""
                    name = when (userNameEditText.text.toString()) {
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
                            userNameEditText.text.toString()
                        }
                    }

                    compareStats(name)

                }
            }
        //
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

        val check : Boolean = "#" in name

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
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) // There are 3 styles, You'll figure it out :)
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
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER) // There are 3 styles, You'll figure it out :)
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
            val matchHistoryURL = "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID"

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
                                builder.setTitle("Here are the last 5 matches!")
                                builder.setItems(
                                    arrayOf<CharSequence>(
                                        matches[0],
                                        matches[1],
                                        matches[2],
                                        matches[3],
                                        matches[4]
                                    )
                                ) { _, which ->
                                    when (which) {
                                        0 -> matchActivityStart(Name, ID, 0)
                                        1 -> matchActivityStart(Name, ID, 1)
                                        2 -> matchActivityStart(Name, ID, 2)
                                        3 -> matchActivityStart(Name, ID, 3)
                                        4 -> matchActivityStart(Name, ID, 4)
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

    private fun compareStats(RiotName: String){

        val nameSplit = RiotName.split("#")

        val Name = nameSplit[0]
        val ID = nameSplit[1]


        //val intent = Intent(this, CompareActivity::class.java)

        startActivity(Intent(this, CompareActivity::class.java))


    }




}