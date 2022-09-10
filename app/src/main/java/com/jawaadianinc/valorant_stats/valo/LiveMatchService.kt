package com.jawaadianinc.valorant_stats.valo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.databases.MatchDatabase
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.jawaadianinc.valorant_stats.valo.match_info.MatchHistoryActivity
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL
import java.util.*

class LiveMatchService : Service() {
    private var gameStart = ""
    private var region = ""
    private var puuid = ""
    private var notificationID = 2
    private var matchID = ""

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        gameStart = intent.extras?.getString("gameStart")!!
        puuid = intent.extras?.getString("puuid")!!
        region = intent.extras?.getString("region")!!
        val key = intent.extras?.getString("key")
        matchID = intent.extras?.getString("matchID")!!

        Thread {
            while (true) {
                Thread.sleep(3000)
                doAsync {
                    try {
                        val url =
                            "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
                        val response =
                            JSONObject(URL(url).readText()).getJSONArray("history")
                                .get(0) as JSONObject
                        val timeStarted = response.getString("gameStartTimeMillis")
                        matchID = response.getString("matchId")
                        Log.d("LiveMatchService", "Checking for game")

                        if (timeStarted != gameStart) {
                            val game =
                                JSONObject(URL("https://$region.api.riotgames.com/val/match/v1/matches/$matchID?api_key=$key").readText())
                            Log.d("LiveMatchService", "Found New Game")
                            val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"
                            val matchJSON = henrikAPI(matchURl)
                            val matchDB = MatchDatabase(this@LiveMatchService)
                            if (!matchDB.insertMatch(matchID, matchJSON.toString())) {
                                Log.d("MatchDatabase", "Match Database Insert Error")
                            }
                            var mode = game.getJSONObject("matchInfo").getString("queueId")
                            mode = when (mode) {
                                "" -> {
                                    "Custom Game"
                                }
                                else -> {
                                    mode.replaceFirstChar {
                                        if (it.isLowerCase()) it.titlecase(
                                            Locale.getDefault()
                                        ) else it.toString()
                                    }
                                }
                            }
                            uiThread {
                                sendNotification(
                                    "GG on that $mode match",
                                    notificationID, matchID
                                )
                                gameStart = timeStarted
                                notificationID++

                                // update the widget
                                val intent =
                                    Intent(this@LiveMatchService, LastMatchWidget::class.java)
                                intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                                val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                                    ComponentName(applicationContext, LastMatchWidget::class.java)
                                )
                                intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                                sendBroadcast(intent)
                            }
                        }
                    } catch (e: Exception) {
                        Log.d("LiveMatch", "Error: $e")
                    }
                }
            }
        }.start()

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            "live_matches_service",
            "Live Matches Service",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationManager.createNotificationChannel(notificationChannel)
        val notification = NotificationCompat.Builder(this, "live_matches_service")
            .setContentTitle("Match Notifications")
            .setContentText("Service is running")
            .setSmallIcon(R.drawable.just_statics)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()

        startForeground(1, notification)

        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun sendNotification(title: String, id: Int, matchID: String) {
        val playerName = PlayerDatabase(this).getPlayerName()

        val intent = Intent(this, MatchHistoryActivity::class.java)
        intent.putExtra("RiotName", playerName!!.split("#")[0])
        intent.putExtra("RiotTag", playerName.split("#")[1])
        intent.putExtra("MatchNumber", 0)
        intent.putExtra("MatchID", matchID)
        val pendantIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationChannel = NotificationChannel(
            "live_matches_details",
            "Live Matches Details",
            NotificationManager.IMPORTANCE_DEFAULT
        )
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)
        notificationManager.createNotificationChannel(notificationChannel)
        val notification = NotificationCompat.Builder(this, "live_matches_details")
            .setContentTitle(title)
            .setContentText("Click here to see your epic game stats")
            .setSmallIcon(R.drawable.fulllogo)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendantIntent)
            .build()
        notificationManager.notify(id, notification)
    }

    private fun henrikAPI(playerURL: String): JSONObject {
        return executeRequest(playerURL)
    }

    private fun executeRequest(playerURL: String): JSONObject {
        val client = OkHttpClient()
        val urlBuilder: HttpUrl.Builder =
            playerURL.toHttpUrlOrNull()!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "HDEV-67e86af9-8bf9-4f6d-b628-f4521b20d772")
            .build()
        val call = client.newCall(request).execute()
        // log the call headers
        // Log.d("Henrik", call.headers.toString())
        return JSONObject(call.body.string())
    }

}
