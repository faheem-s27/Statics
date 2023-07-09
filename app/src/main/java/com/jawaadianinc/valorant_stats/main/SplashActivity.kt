package com.jawaadianinc.valorant_stats.main

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.jawaadianinc.valorant_stats.R
import java.util.Locale


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        val languageCode = Locale.getDefault().language
        setLocale(languageCode)
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
