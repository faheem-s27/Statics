package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)

        val mHandler = Handler()
        mHandler.postDelayed(Runnable {
            val intent = Intent(this, FindAccount::class.java)
            startActivity(intent)
            finish()
        }, 2000L)

    }
}