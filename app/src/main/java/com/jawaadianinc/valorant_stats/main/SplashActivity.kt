package com.jawaadianinc.valorant_stats.main

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.jawaadianinc.valorant_stats.R

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        startActivity(Intent(this, LoadingActivity::class.java))
        //overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        finish()
    }
}
