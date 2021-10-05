package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout

class MatchHistoryActivity : AppCompatActivity() {

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_history)

        tabLayout = findViewById<View>(R.id.tabsforMatch) as? TabLayout
        viewPager = findViewById<View>(R.id.view_pager_matchHistory) as? ViewPager
        tabLayout?.tabGravity = TabLayout.GRAVITY_CENTER
        tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
        tabLayout?.newTab()?.setText("Stats")?.setIcon(R.drawable.stats_icon).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        tabLayout?.newTab()?.setText("Players")?.setIcon(R.drawable.players_icon).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        tabLayout?.newTab()?.setText("Rounds")?.setIcon(R.drawable.rounds_icno).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        val adapter = tabLayout?.tabCount?.let { MatchHistoryAdapter(supportFragmentManager, it) }
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

        val title : TextView = findViewById(R.id.title)
        val Name: String? = intent.getStringExtra("RiotName")
        title.text = "Match Information"


        val refresFab : FloatingActionButton = findViewById(R.id.refreshFab2)
        refresFab.setOnClickListener{
            finish()
            startActivity(Intent(this, FindAccount::class.java))
        }

    }
}