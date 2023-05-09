package com.jawaadianinc.valorant_stats.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.BuildConfig
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.Henrik
import com.jawaadianinc.valorant_stats.valo.activities.LoggingInActivityRSO
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.StaticsMainActivity
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class LoadingActivity : AppCompatActivity() {
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var updateText: TextView
    private lateinit var backgroundIMG: ImageView
    private lateinit var videoPlayer: VideoView
    private var key = ""
    //private var totalCount by Delegates.notNull<Int>()

    lateinit var assetsDB: AssetsDatabase

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        FirebaseApp.initializeApp(/*context=*/this)

        assetsDB = AssetsDatabase(this)

        loadingProgressBar = findViewById(R.id.progressBar4)
        updateText = findViewById(R.id.textView4)
        backgroundIMG = findViewById(R.id.imageView7)
        videoPlayer = findViewById(R.id.videoView3)

        key = BuildConfig.RIOT_API_KEY

        loadUI()

        // check if network is available, if not, show a dialog box to the user that will have an option to retry
        if (!isNetworkAvailable()) {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Error!")
            builder.setMessage("Statics requires an active internet connection!")
            builder.setPositiveButton("Retry") { _, _ ->
                recreate()
            }
            builder.setNegativeButton("Exit") { _, _ ->
                finish()
            }
            builder.setCancelable(false)
            builder.show()
        } else {
            addAssetsToDatabase()
        }
    }

    private fun loadUI() {
        loadingProgressBar.alpha = 0.0f
        updateText.alpha = 0.0f

        loadingProgressBar.translationY = +100f
        updateText.translationY = +100f

        loadingProgressBar.animate().alpha(1f).translationYBy(-100f).setInterpolator {
            it * it * it * (it * (it * 6 - 15) + 10)
        }.duration = 1000
        updateText.animate().alpha(1f).translationYBy(-100f).setInterpolator {
            it * it * it * (it * (it * 6 - 15) + 10)
        }.duration = 1000

        backgroundIMG.alpha = 0f
        backgroundIMG.animate().setDuration(1500).alpha(1f).setInterpolator {
            it * it * it * (it * (it * 6 - 15) + 10)
        }.start()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    private fun addAssetsFromJson(jsonArray: JSONArray, type: String) = runBlocking {
        withContext(Dispatchers.IO) {
            // for each json
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                var UUID = ""
                var name = ""
                var imageString = ""
                var bitmap: Bitmap? = null

                if (type == "Agent") {
                    name = jsonObject.getString("displayName")
                    UUID = jsonObject.getString("uuid")
                    imageString = jsonObject.getString("displayIcon")
                }
                if (type == "Map") {
                    name = jsonObject.getString("displayName")
                    UUID = jsonObject.getString("uuid")
                    imageString = jsonObject.getString("listViewIcon")
                }
                if (type == "Title") {
                    name = jsonObject.getString("titleText")
                    UUID = jsonObject.getString("uuid")
                    bitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                }

                // if bitmap is null, get bitmap from image string using Picasso
                if (bitmap == null) {
                    bitmap = Picasso.get().load(imageString).get()
                }

                assetsDB.checkAndAddData(UUID, type, name, bitmap!!, assetsDB)
                withContext(Dispatchers.Main) {
                    //updateText.text = "Loading $type: $name"
                }
                // Log.d("AssetsDatabase", "Got type: $type, name: $name, UUID: $UUID, imageString: $imageString and bitmap: $bitmap")
            }
        }
    }

    private fun addAssetsToDatabase() {
        // function to download the agents and map images to database
        val agentURL = "https://valorant-api.com/v1/agents?isPlayableCharacter=true"
        val mapURL = "https://valorant-api.com/v1/maps"
        val titlesURL = "https://valorant-api.com/v1/playertitles"

        val dbCount = assetsDB.getNumberOfRows()
        if (dbCount == 0) {
            // write me a toast message
            Toast.makeText(
                this@LoadingActivity,
                "(first time load)",
                Toast.LENGTH_LONG
            ).show()
        }

        updateText.text = "Connecting to Riot"

        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
        coroutineScope.launch {
            // make a hashmap of the URLs and the type of data
            val hashData = hashMapOf(agentURL to "Agent", mapURL to "Map", titlesURL to "Title")
            // create a list of jobs with the hashmap
            val progress = 100 / hashData.size
            val jobs = hashData.map { (url, type) ->
                async(Dispatchers.IO) {
                    val json = JSONObject(URL(url).readText())
                    val data = json.getJSONArray("data")
                    addAssetsFromJson(data, type)
                    withContext(Dispatchers.Main) {
                        loadingProgressBar.progress += progress
                    }
                }
            }
            jobs.awaitAll()
            assetsDB.close()

            updateText.text = "Starting"
            val valoName = PlayerDatabase(this@LoadingActivity).getPlayerName()
            valoAccountStats(valoName, key)
        }
    }

    private fun valoAccountStats(valoName: String?, key: String) {
        if (valoName == null) {
            // check if video is already played
            val prefs = getSharedPreferences("prefs", MODE_PRIVATE)
            val firstStart = prefs.getBoolean("firstStart", true)
            if (firstStart) {
                val editor = prefs.edit()
                editor.putBoolean("firstStart", false)
                editor.apply()
                // play video
                videoPlayer.visibility = View.VISIBLE
                val videoPath = "android.resource://" + packageName + "/" + R.raw.staticsintro
                // set the path of the video to be played
                videoPlayer.setVideoPath(videoPath)
                videoPlayer.setOnCompletionListener {
                    val intent = Intent(this, LoggingInActivityRSO::class.java)
                    intent.putExtra("key", key)
                    startActivity(intent)
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                    finish()
                }
                videoPlayer.setOnPreparedListener {
                    videoPlayer.start()
                }
            } else {
                val intent = Intent(this, LoggingInActivityRSO::class.java)
                intent.putExtra("key", key)
                startActivity(intent)
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                finish()
            }

        } else {
            val PUUID =
                PlayerDatabase(this).getPUUID(valoName.split("#")[0], valoName.split("#")[1])
            val intent = Intent(this, StaticsMainActivity::class.java)
            intent.putExtra("key", key)
            intent.putExtra("region", PlayerDatabase(this).getRegion(PUUID))
            intent.putExtra("playerName", valoName)
            intent.putExtra("playerImageID", getPlayerImage(valoName))

            backgroundIMG.animate().setDuration(1000).alpha(0f).translationY(-100f)
                .setInterpolator {
                    it * it * it * (it * (it * 6 - 15) + 10)
                }.start()
            updateText.text = "Loading $valoName's stats"
            updateText.animate().setDuration(1000).alpha(0f).translationY(-100f)
                .setInterpolator {
                    it * it * it * (it * (it * 6 - 15) + 10)
                }.start()
            // fade away the progressbar with velocity upwards
            loadingProgressBar.animate().setDuration(1000).alpha(0f).translationY(-100f)
                .setInterpolator {
                    it * it * it * (it * (it * 6 - 15) + 10)
                }.withEndAction {
                    startActivity(intent)
                    overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                    finish()
                }
        }
    }

    private fun getPlayerImage(valoName: String?): String {
        if (valoName == null) return "9fb348bc-41a0-91ad-8a3e-818035c4e561"
        // run on main thread blocking
        return runBlocking(Dispatchers.IO) {
            try {
                val url =
                    "https://api.henrikdev.xyz/valorant/v1/account/${valoName.split("#")[0]}/${
                        valoName.split("#")[1]
                    }"
                val json = Henrik(this@LoadingActivity).henrikAPI(url)
                val playerAccount = Gson().fromJson(json.toString(), Account::class.java)
                return@runBlocking playerAccount.data.card.id
            } catch (e: Exception) {
                Log.d("LoadingActivity", "Error getting player image: $e")
                return@runBlocking "9fb348bc-41a0-91ad-8a3e-818035c4e561"
            }
        }
    }

}
