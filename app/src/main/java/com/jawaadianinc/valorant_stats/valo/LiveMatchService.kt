package com.jawaadianinc.valorant_stats.valo

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.jawaadianinc.valorant_stats.R
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

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        gameStart = intent.extras?.getString("gameStart")!!
        puuid = intent.extras?.getString("puuid")!!
        region = intent.extras?.getString("region")!!
        val key = intent.extras?.getString("key")

        Thread {
            while (true) {
                Thread.sleep(5000)
                doAsync {
                    try {
                        val URL =
                            "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/${puuid}?api_key=${key}"
                        val response =
                            JSONObject(URL(URL).readText()).getJSONArray("history")
                                .get(0) as JSONObject
                        val timeStarted = response.getString("gameStartTimeMillis")
                        val matchID = response.getString("matchId")

                        if (timeStarted != gameStart) {
                            val game =
                                JSONObject(URL("https://$region.api.riotgames.com/val/match/v1/matches/$matchID?api_key=$key").readText())
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
                                    "A match was detected!",
                                    "You just played $mode, tap to see details",
                                    notificationID
                                )
                                gameStart = timeStarted
                                notificationID++
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
            .setContentTitle("Match Notifications Service")
            .setContentText("If you see this, the match detection service is working")
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

    private fun sendNotification(title: String, message: String, id: Int) {
        val intent = Intent(this, ViewMatches::class.java)
        intent.putExtra("Region", region)
        intent.putExtra("PUUID", puuid)
        intent.putExtra("NumberOfMatches", 1)
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
            .setContentText(message)
            .setSmallIcon(R.drawable.just_statics)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendantIntent)
            .build()
        notificationManager.notify(id, notification)
    }

}
