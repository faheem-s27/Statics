package com.jawaadianinc.valorant_stats

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout

class brawlStatsActivity : AppCompatActivity() {

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brawl_stats)

        val brawlName = intent.extras!!.getString("BrawlName")
        val brawlID = intent.extras!!.getString("BrawlID")

        val title: TextView = findViewById(R.id.title)

        tabLayout = findViewById<View>(R.id.tabs) as? TabLayout
        viewPager = findViewById<View>(R.id.view_pager) as? ViewPager
        tabLayout?.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
        tabLayout?.newTab()?.setText("Stats")?.setIcon(R.drawable.newcompicon).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        tabLayout?.newTab()?.setText("Ranked")?.setIcon(R.drawable.compicon).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        tabLayout?.newTab()?.setText("2v2 Ranked")?.setIcon(R.drawable.spikerushicon).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }

        tabLayout?.newTab()?.setText("Clan")?.setIcon(R.drawable.deathmatch).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }

        val adapter = tabLayout?.tabCount?.let { BrawlAdapter(supportFragmentManager, it) }
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

        title.text = "Brawlhalla stats for $brawlName ($brawlID)"


    }
}
