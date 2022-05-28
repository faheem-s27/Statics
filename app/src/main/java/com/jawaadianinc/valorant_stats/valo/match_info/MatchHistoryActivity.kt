package com.jawaadianinc.valorant_stats.valo.match_info

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.ZoomOutPageTransformer
import com.jawaadianinc.valorant_stats.valo.adapters.MatchHistoryAdapter
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class MatchHistoryActivity : AppCompatActivity() {

    private var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_match_history)

        val refresFab: FloatingActionButton = findViewById(R.id.refreshFab2)
        val Name = intent.extras!!.getString("RiotName")
        val ID = intent.extras!!.getString("RiotID")
        val MatchNumber = intent.extras!!.getInt("MatchNumber")
        val IDofMatch = intent.extras!!.getString("MatchID")
        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID?size=10"

        // show progress bar while loading
        val progressBar = ProgressBar(this)
        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = true

//        val progressDialog = ProgressDialog(this)
//        progressDialog.setTitle("Getting information")
//        progressDialog.setMessage("Collecting match details!")
//        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
//        progressDialog.setCancelable(false)
//        progressDialog.show()

        doAsync {
            val matchID: String = if (IDofMatch == "none") {
                val matchhistoryURL = URL(allmatches).readText()
                val jsonMatches = JSONObject(matchhistoryURL)
                val data = jsonMatches["data"] as JSONArray
                val easier = data.getJSONObject(MatchNumber).getJSONObject("metadata")
                easier.getString("matchid")
            } else {
                IDofMatch!!
            }
            val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"
            matchJSON = JSONObject(URL(matchURl).readText())
            val metadata = matchJSON!!.getJSONObject("data").getJSONObject("metadata")
            val map = metadata.getString("map")
            val mapJSON =
                JSONObject(URL("https://valorant-api.com/v1/maps").readText()).getJSONArray("data")
            for (i in 0 until mapJSON.length()) {
                val currentMap = mapJSON.getJSONObject(i)
                val name = currentMap.getString("displayName")
                if (name == map) {
                    mapURL = currentMap.getString("displayIcon")
                    break
                }
            }

            uiThread {
                //progressDialog.dismiss()
                progressBar.visibility = View.GONE
                tabLayout = findViewById<View>(R.id.tabsforMatch) as? TabLayout
                viewPager = findViewById<View>(R.id.view_pager_matchHistory) as? ViewPager
                tabLayout?.tabGravity = TabLayout.GRAVITY_CENTER
                tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
                tabLayout?.newTab()?.setText("Details")?.setIcon(R.drawable.details).let {
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

                tabLayout?.newTab()?.setText("Spike Map")?.setIcon(R.drawable.spikeicon).let {
                    if (it != null) {
                        tabLayout?.addTab(it)
                    }
                }

                tabLayout?.newTab()?.setText("Kill Map")?.setIcon(R.drawable.killmapicon).let {
                    if (it != null) {
                        tabLayout?.addTab(it)
                    }
                }

                tabLayout?.newTab()?.setText("Kill Feed")?.setIcon(R.drawable.killicon).let {
                    if (it != null) {
                        tabLayout?.addTab(it)
                    }
                }

                val adapter =
                    tabLayout?.tabCount?.let { MatchHistoryAdapter(supportFragmentManager, it) }
                viewPager?.adapter = adapter
                viewPager?.offscreenPageLimit = 5
                viewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
                viewPager?.setPageTransformer(true, ZoomOutPageTransformer())
                tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        viewPager?.currentItem = tab.position
                        if (tab.position == 5) {
                            refresFab.hide()
                        } else {
                            refresFab.show()
                        }
                    }

                    override fun onTabUnselected(tab: TabLayout.Tab) {

                    }

                    override fun onTabReselected(tab: TabLayout.Tab) {

                    }
                })

                val title: TextView = findViewById(R.id.title)
                title.text = "Match Information"


                refresFab.setOnClickListener {
                    finish()
                }
            }
        }
    }

    companion object {
        var matchJSON: JSONObject? = null
        var mapURL: String? = null
    }
}
