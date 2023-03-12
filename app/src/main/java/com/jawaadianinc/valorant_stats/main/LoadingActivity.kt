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
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.StaticsMainActivity
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL
import kotlin.properties.Delegates

class LoadingActivity : AppCompatActivity() {
    private lateinit var loadingProgressBar: ProgressBar
    private lateinit var updateText: TextView
    private lateinit var backgroundIMG: ImageView
    private lateinit var videoPlayer: VideoView
    private var key = ""
    private var totalCount by Delegates.notNull<Int>()

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)
//        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        loadingProgressBar = findViewById(R.id.progressBar4)
        updateText = findViewById(R.id.textView4)
        backgroundIMG = findViewById(R.id.imageView7)
        videoPlayer = findViewById(R.id.videoView3)

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
            FirebaseApp.initializeApp(/*context=*/this)
            FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
                SafetyNetAppCheckProviderFactory.getInstance()
            )
            val database = Firebase.database
            val playersRef = database.getReference("VALORANT/key")

            playersRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    // Log.d("Statics", "Key is: " + dataSnapshot.value)
                    key = (dataSnapshot.value as String?).toString()
                    addAssetsToDatabase()
                }

                override fun onCancelled(databaseError: DatabaseError) {
                    //loadingProgressBar.visibility = View.GONE
                    updateText.text = "An error occurred while connecting to Statics :("
                }

            })
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
        updateText.text = "Checking connection"
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    private fun addAssetsToDatabase() {
        val assetsDB = AssetsDatabase(this@LoadingActivity)
        val dbCount = assetsDB.getNumberOfRows()
        if (dbCount == 0) {
            // write me a toast message
            Toast.makeText(
                this@LoadingActivity,
                "Loading resources (first time load)",
                Toast.LENGTH_LONG
            ).show()
        }

        updateText.text = "Connecting to Riot"

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

                totalCount = agentData.length() + mapData.length() + titlesData.length()

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
                                                updateResourcesText()
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
                        Thread.sleep(200)
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
                                                    updateResourcesText()
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
                        Thread.sleep(200)
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
                                    updateResourcesText()
                                }
                            } else {
                                Log.d(
                                    "AssetsDatabase",
                                    "Failed to add $titleName details"
                                )
                            }
                            Thread.sleep(200)
                        }
                    }
                }

                uiThread {
                    if (dbCount == totalCount) {
                        updateText.text = "Starting"
                        val valoName = PlayerDatabase(this@LoadingActivity).getPlayerName()
                        // remove the builder
                        valoAccountStats(valoName, key)
                    } else {
                        updateText.text = "Retrying"
                        addAssetsToDatabase()
                    }
                }

            } catch (e: Exception) {
                uiThread {
                    val dialog = AlertDialog.Builder(this@LoadingActivity)
                    dialog.setTitle("Error while loading assets")
                    dialog.setMessage("Error occurred trying to retrieve Valorant assets\nPress ok to restart the app\n\nIf the problem persists, please contact the developer on discord.\nError: $e")
                    dialog.setPositiveButton("OK") { _, _ ->
                        // exit the app
                        recreate()
                    }
                    dialog.show()
                    // Log to firebase database
                    val database = Firebase.database
                    val myRef = database.getReference("Error")
                    myRef.child("ErrorLoadAssets").setValue(e.toString())
                    //Log.d("AssetsDatabase", "Error downloading assets : $e")
                }
            }
        }
    }


    private fun valoAccountStats(valoName: String?, key: String) {
        if (valoName == null) {
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
            val PUUID =
                PlayerDatabase(this).getPUUID(valoName.split("#")[0], valoName.split("#")[1])
            val intent = Intent(this, StaticsMainActivity::class.java)

            // TESTING PLAYERS


            intent.putExtra("key", key)
            intent.putExtra("region", PlayerDatabase(this).getRegion(PUUID))
            intent.putExtra("playerName", valoName)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            finish()
        }
    }

    fun updateResourcesText() {
        // get the current number of resources
        val dbCount = AssetsDatabase(this).getNumberOfRows()
        updateText.text = "Downloading $dbCount/$totalCount assets"
    }
}
