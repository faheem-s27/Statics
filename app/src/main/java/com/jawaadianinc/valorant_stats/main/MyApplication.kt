package com.jawaadianinc.valorant_stats.main

import android.app.Application
import android.content.res.Configuration
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Locale


class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        FirebaseApp.initializeApp(this);
        FirebaseMessaging.getInstance().subscribeToTopic("chat_notification")
            .addOnCompleteListener {
            }

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result
            // send this token to server
        })

        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val firstTimeSetUp = sharedPreferences.getBoolean("dark_mode_first", false)
        if (!firstTimeSetUp) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
            val editor = sharedPreferences.edit()
            editor.putBoolean("dark_mode_first", true)
            editor.apply()
        } else {
            val darkModeEnabled = sharedPreferences.getBoolean("dark_mode", false)
            if (darkModeEnabled) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            }
        }


        // Set language
        val selectedLanguage = sharedPreferences.getString("language", "en")
        updateLanguage(selectedLanguage!!)
    }

    fun updateLocale(language: String) {
        val locale = Locale(language)
        Locale.setDefault(locale)

        val configuration = Configuration()
        configuration.locale = locale

        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    fun updateLanguage(language: String) {
        val config = resources.configuration
        val locale = Locale(language)
        Locale.setDefault(locale)
        config.locale = locale
        resources.updateConfiguration(config, resources.displayMetrics)
    }

}
