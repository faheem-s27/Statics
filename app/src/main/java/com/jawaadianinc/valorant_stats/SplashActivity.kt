package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        startActivity(Intent(this, GamePicker::class.java))
        finish()

//        val mHandler = Handler()
//        mHandler.postDelayed(Runnable {
//            val intent = Intent(this, GamePicker::class.java)
//            startActivity(intent)
//            finish()
//        }, 2000L)

    }
}
