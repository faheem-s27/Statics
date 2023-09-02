package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.pm.PackageInfo
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jawaadianinc.valorant_stats.R

class NewAbout : AppCompatActivity() {
    private lateinit var descTextView: TextView

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

        playstoreFAB.setOnClickListener {
            val playstore =
                "https://play.google.com/store/apps/details?id=com.jawaadianinc.valorant_stats"
            val intent = android.content.Intent(
                android.content.Intent.ACTION_VIEW,
                android.net.Uri.parse(playstore)
            )
            startActivity(intent)
        }

        // get the shared preferences for LatestFeatureDescription
        val sharedPref = getSharedPreferences("LatestFeature", MODE_PRIVATE)
        val latestFeatureDescription =
            sharedPref.getString("LatestFeatureDescription", "No description available")

        val lenny = """¯\_(ツ)_/¯"""
        descTextView = findViewById<TextView>(R.id.aboutDesc)
        descTextView.text = "Hi! I'm the developer of Statics\n " +
                "A Valorant Stats Tracker app, for people who want to see their Valorant stats on mobile more conveniently without using an external browser or PC\n" +
                lenny + "\n" + "Out of love for Valorant and with my coding expertise\nI created this app" +
                " for fun and has been mostly a hobby project, but I'm always open to suggestions and feedback\nLots of love ❤️ \nFaheem Saleem\nMy Valorant user is Duck#2004"
        descTextView.gravity = android.view.Gravity.CENTER

        val textFile = assets.open("TranslatersPeople.txt").bufferedReader().use { it.readText() }
        val TranslaterPeople = ArrayList<Translater>()
        val lines = textFile.split("\n")
        for (line in lines) {
            val parts = line.split(",")
            val name = parts[0]
            val discord = parts[1]
            val language = parts[2]
            val image = parts[3]
            TranslaterPeople.add(Translater(name, discord, language, image))
        }
        val adapter = TranslaterAdapter(TranslaterPeople)
        val listView = findViewById<RecyclerView>(R.id.listViewTranslaters)
        listView.layoutManager = GridLayoutManager(this, 2)
        listView.adapter = adapter

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}
