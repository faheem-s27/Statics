package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.net.URL


class StatsActivity : AppCompatActivity() {
    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)

        tabLayout = findViewById<View>(R.id.tabs) as? TabLayout
        viewPager = findViewById<View>(R.id.view_pager) as? ViewPager
        tabLayout?.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
        tabLayout?.newTab()?.setText("Competitive")?.setIcon(R.drawable.newcompicon).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        tabLayout?.newTab()?.setText("Unrated")?.setIcon(R.drawable.compicon).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        tabLayout?.newTab()?.setText("SpikeRush")?.setIcon(R.drawable.spikerushicon).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }

        tabLayout?.newTab()?.setText("Deathmatch")?.setIcon(R.drawable.deathmatch).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }

        val adapter = tabLayout?.tabCount?.let { StatsAdaptor(supportFragmentManager, it) }
        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = 3
        viewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        viewPager?.setPageTransformer(true, ZoomOutPageTransformer())
        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager?.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        val title: TextView = findViewById(R.id.title)
        val Name: String? = intent.getStringExtra("RiotName")
        val ID: String? = intent.getStringExtra("RiotID")

        val player_background: ImageView = findViewById(R.id.playerBackgroundMain)
        val PlayerURL = "https://api.henrikdev.xyz/valorant/v1/account/$Name/$ID?force=true"

        doAsync {
            val text = URL(PlayerURL).readText()
            var data = JSONObject(text)
            data = data["data"] as JSONObject
            val cards = data["card"] as JSONObject
            val playerCard = cards["large"].toString()
            runOnUiThread {
                Picasso
                    .get()
                    .load(playerCard)
                    .fit()
                    .centerCrop()
                    .into(player_background)
                player_background.alpha = 0.2f

            }
        }
        title.text = "Stats for $Name"

        val refresFab: FloatingActionButton = findViewById(R.id.refreshFab)
        refresFab.setOnClickListener {
            finish()
            startActivity(Intent(this, FindAccount::class.java))
        }

    }
}