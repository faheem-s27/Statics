package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.R
import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton


class NewAbout : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(com.jawaadianinc.valorant_stats.R.layout.activity_new_about)

        val toolbar: Toolbar = findViewById(com.jawaadianinc.valorant_stats.R.id.new_about_toolbar)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.setDisplayShowHomeEnabled(true)

        supportActionBar!!.title = "About";

        val pInfo: PackageInfo =
            this.packageManager.getPackageInfo(this.packageName, 0)
        val versionName = pInfo.versionName
        val versionCode = pInfo.versionCode

        val versionText =
            findViewById<TextView>(com.jawaadianinc.valorant_stats.R.id.new_about_versionName)
        val discordFAB =
            findViewById<FloatingActionButton>(com.jawaadianinc.valorant_stats.R.id.new_about_discord)
        val instaFAB =
            findViewById<FloatingActionButton>(com.jawaadianinc.valorant_stats.R.id.new_about_insta)
        val playstoreFAB =
            findViewById<FloatingActionButton>(com.jawaadianinc.valorant_stats.R.id.new_about_playstore)

        versionText.text = "$versionName ($versionCode)"

        discordFAB.setOnClickListener {
            val discord = "https://discord.gg/jwfJUQMPP7"
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(discord)
            )
            startActivity(intent)
        }

        instaFAB.setOnClickListener {
            val insta = "https://www.instagram.com/faheem.s27/"
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(insta)
            )
            startActivity(intent)
        }

        playstoreFAB.setOnClickListener {
            val playstore =
                "https://play.google.com/store/apps/details?id=com.jawaadianinc.valorant_stats"
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(playstore)
            )
            startActivity(intent)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
