package com.jawaadianinc.valorant_stats.valo.cosmetics.weapon

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.main.ZoomOutPageTransformer

class WeaponActivity : AppCompatActivity() {

    private var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_history)

        val weaponName = intent.getStringExtra("weaponName")

        val refresFab: FloatingActionButton = findViewById(R.id.refreshFab2)
        tabLayout = findViewById<View>(R.id.tabsforMatch) as? TabLayout
        viewPager = findViewById<View>(R.id.view_pager_matchHistory) as? ViewPager
        tabLayout?.tabGravity = TabLayout.GRAVITY_CENTER
        tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE

        tabLayout?.newTab()?.setText("Stats")?.setIcon(R.drawable.stats).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        tabLayout?.newTab()?.setText("Videos")?.setIcon(R.drawable.shop).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }
        tabLayout?.newTab()?.setText("Skins")?.setIcon(R.drawable.skins).let {
            if (it != null) {
                tabLayout?.addTab(it)
            }
        }


        val adapter =
            tabLayout?.tabCount?.let { WeaponAdapter(supportFragmentManager, it) }
        viewPager?.adapter = adapter
        viewPager?.offscreenPageLimit = 5
        viewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
        viewPager?.setPageTransformer(true, ZoomOutPageTransformer())

        tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager?.currentItem = tab.position
//                if (tab.position == 5) {
//                    refresFab.hide()
//                } else {
//                    refresFab.show()
//                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }
        })

        val title: TextView = findViewById(R.id.title)
        title.text = "$weaponName Information"


        refresFab.setOnClickListener {
            finish()
        }

    }

}
