package com.jawaadianinc.valorant_stats.main

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.ConnectivityManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.LoggingInActivityRSO
import com.jawaadianinc.valorant_stats.valo.activities.ValorantMainMenu
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL


class LoadingActivity : AppCompatActivity() {

    private var key = ""
    //private var urlStaticsIntro = "https://firebasestorage.googleapis.com/v0/b/statics-fd699.appspot.com/o/staticsIntroPortraitNoot.mp4?alt=media&token=d093d222-84f5-4af9-bc0d-2cfa32d6d53c"

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val loadingProgressBar: ProgressBar = findViewById(R.id.progressBar4)
        val updateText: TextView = findViewById(R.id.textView4)

        loadingProgressBar.alpha = 0.0f
        updateText.alpha = 0.0f

        loadingProgressBar.translationY = +100f
        updateText.translationY = +100f

        loadingProgressBar.animate().alpha(1f).translationYBy(-100f).duration = 1000
        updateText.animate().alpha(1f).translationYBy(-100f).duration = 1000

        if (!isNetworkAvailable()) {
            loadingProgressBar.visibility = View.INVISIBLE
            updateText.text = "Error!\nStatics requires an active internet connection!"
        }

        val backgroundIMG = findViewById<ImageView>(R.id.imageView7)
        backgroundIMG.alpha = 0f
        backgroundIMG.animate().setDuration(1500).alpha(1f).setInterpolator {
            it * it * it * (it * (it * 6 - 15) + 10)
        }.start()



        FirebaseApp.initializeApp(/*context=*/this)
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/key")

        playersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                key = (dataSnapshot.value as String?).toString()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@LoadingActivity,
                    "Failed to connect to Statics!",
                    Toast.LENGTH_SHORT
                ).show()
                loadingProgressBar.visibility = View.GONE
                updateText.text = "An error occurred while connecting to Statics :("
            }
        })

        addAssetsToDatabase()
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    private fun addAssetsToDatabase() {
        val updateText: TextView = findViewById(R.id.textView4)
        val assetsDB = AssetsDatabase(this@LoadingActivity)
        val dbCount = assetsDB.getNumberOfRows()

        updateText.text = "Loading resources"

        doAsync {
            try {
                // function to download the agents and map images to database
                val agentURL = "https://valorant-api.com/v1/agents?isPlayableCharacter=true"
                val mapURL = "https://valorant-api.com/v1/maps"
                val titlesURL = "https://valorant-api.com/v1/playertitles"

                val agentJSON = JSONObject(URL(agentURL).readText())
                val mapJSON = JSONObject(URL(mapURL).readText())
                val titlesJSON = JSONObject(URL(titlesURL).readText())

                val agentData = agentJSON.getJSONArray("data")
                val mapData = mapJSON.getJSONArray("data")
                val titlesData = titlesJSON.getJSONArray("data")

                val totalCount = agentData.length() + mapData.length() + titlesData.length()

                if (totalCount != dbCount) {
                    for (i in 0 until agentData.length()) {
                        val currentAgent = agentData.getJSONObject(i)
                        val agentName = currentAgent.getString("displayName")
                        val agentUUID = currentAgent.getString("uuid")
                        val agentImage = currentAgent.getString("displayIcon")
                        uiThread {
                            Picasso.get().load(agentImage)
                                .into(object : com.squareup.picasso.Target {
                                    override fun onBitmapLoaded(
                                        agentBitMap: Bitmap?,
                                        from: Picasso.LoadedFrom?
                                    ) {
                                        if (!assetsDB.checkForExisting(agentUUID, agentName)) {
                                            if (assetsDB.addData(
                                                    agentUUID,
                                                    "Agent",
                                                    agentName,
                                                    agentBitMap!!
                                                )
                                            ) {
                                                updateText.text = "Added $agentName"
                                            } else {
                                                Log.d(
                                                    "AssetsDatabase",
                                                    "Failed to add $agentName details"
                                                )
                                            }
                                        }
                                    }

                                    override fun onBitmapFailed(
                                        e: java.lang.Exception?,
                                        errorDrawable: Drawable?
                                    ) {
                                        //TODO("Not yet implemented")
                                    }

                                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                        //TODO("Not yet implemented")
                                    }
                                })
                        }
                        Thread.sleep(100)
                    }

                    for (i in 0 until mapData.length()) {
                        val currentMap = mapData.getJSONObject(i)
                        val mapName = currentMap.getString("displayName")
                        val mapUUID = currentMap.getString("uuid")
                        val mapImage = currentMap.getString("listViewIcon")
                        uiThread {
                            Picasso.get().load(mapImage)
                                .into(object : com.squareup.picasso.Target {
                                    override fun onBitmapLoaded(
                                        mapBitMap: Bitmap?,
                                        from: Picasso.LoadedFrom?
                                    ) {
                                        if (!assetsDB.checkForExisting(mapUUID, mapName)) {
                                            if (assetsDB.addData(
                                                    mapUUID,
                                                    "Map",
                                                    mapName,
                                                    mapBitMap!!
                                                )
                                            ) {
                                                uiThread {
                                                    updateText.text = "Added $mapName"
                                                }
                                            } else {
                                                Log.d(
                                                    "AssetsDatabase",
                                                    "Failed to add $mapName details"
                                                )
                                            }
                                        }
                                    }

                                    override fun onBitmapFailed(
                                        e: java.lang.Exception?,
                                        errorDrawable: Drawable?
                                    ) {
                                        //TODO("Not yet implemented")
                                    }

                                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                        //TODO("Not yet implemented")
                                    }
                                })
                        }
                        Thread.sleep(100)
                    }

                    for (i in 0 until titlesData.length()) {
                        val currentTitle = titlesData.getJSONObject(i)
                        val titleName = currentTitle.getString("titleText")
                        val titleUUID = currentTitle.getString("uuid")
                        val emptyBitmap = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)
                        if (!assetsDB.checkForExisting(titleUUID, titleName)) {
                            if (assetsDB.addData(
                                    titleUUID,
                                    "Title",
                                    titleName,
                                    emptyBitmap!!
                                )
                            ) {
                                uiThread {
                                    updateText.text = "Added $titleName"
                                }
                            } else {
                                Log.d(
                                    "AssetsDatabase",
                                    "Failed to add $titleName details"
                                )
                            }
                            Thread.sleep(50)
                        }
                    }
                }

                val finalKey = checkForKey()
                uiThread {
                    updateText.text = "Starting"
                    val valoName = PlayerDatabase(this@LoadingActivity).getPlayerName()
                    valoAccountStats(valoName, finalKey)
                }

            } catch (e: Exception) {
                uiThread {
                    val dialog = AlertDialog.Builder(this@LoadingActivity)
                    dialog.setTitle("Error while loading assets")
                    dialog.setMessage("An unknown error occurred whilst trying to retrieve Valorant assets\nPlease restart the app. \n\nIf the problem persists, please contact the developer.")
                    dialog.setPositiveButton("OK") { _, _ ->
                        // exit the app
                        finish()
                    }
                    dialog.show()
                    Log.d("AssetsDatabase", "Error downloading assets : $e")
                }
            }
        }
    }

    private fun checkForKey(): String {
        while (true) {
            if (key == "") {
                Thread.sleep(500)
            } else {
                return key
            }
        }
    }

    private fun valoAccountStats(valoName: String?, key: String) {
        if (valoName == null) {
            val videoPlayer = findViewById<VideoView>(R.id.videoView3)
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
            val intent = Intent(this, ValorantMainMenu::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            finish()
        }
    }
}
