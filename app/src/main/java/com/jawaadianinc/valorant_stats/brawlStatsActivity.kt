package com.jawaadianinc.valorant_stats

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class brawlStatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brawl_stats)

        val brawlName = intent.extras!!.getString("BrawlName")
        val brawlID = intent.extras!!.getString("BrawlID")

    }
}