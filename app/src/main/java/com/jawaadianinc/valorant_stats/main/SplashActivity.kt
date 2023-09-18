package com.jawaadianinc.valorant_stats.main

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.color.DynamicColors
import com.jawaadianinc.valorant_stats.R
import java.util.Locale


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        DynamicColors.applyToActivitiesIfAvailable(application)

        val languageCode = Locale.getDefault().language
        setLocale(languageCode)

        val name = "Chat Notification"
        val descriptionText = "All the chat notifications will be sent through this channel"
        val importance = NotificationManager.IMPORTANCE_HIGH
        var mChannel = NotificationChannel("chat_notification", name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system. You can't change the importance
        // or other notification behaviors after this.
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)

        val channelID = "match_notification"
        val channelName = "Match Notification"
        val desc = "All the match notifications will be sent through this channel"
        mChannel = NotificationChannel(channelID, channelName, importance)
        mChannel.description = desc
        notificationManager.createNotificationChannel(mChannel)

        startActivity(Intent(this, LoadingActivity::class.java))
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        finish()
    }

    private fun setLocale(languageCode: String?) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        resources.updateConfiguration(config, resources.displayMetrics)
        recreate() // Restart your activity to apply the language change
    }
}
