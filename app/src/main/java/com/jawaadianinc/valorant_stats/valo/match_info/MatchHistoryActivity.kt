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
import com.jawaadianinc.valorant_stats.R.*
import com.jawaadianinc.valorant_stats.main.ZoomOutPageTransformer
import com.jawaadianinc.valorant_stats.valo.Henrik
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
        setContentView(layout.activity_match_history)

        val refresFab: FloatingActionButton =
            findViewById(id.refreshFab2)
        val Name = intent.extras!!.getString("RiotName")
        val ID = intent.extras!!.getString("RiotID")
        val MatchNumber = intent.extras!!.getInt("MatchNumber")
        val IDofMatch = intent.extras!!.getString("MatchID")
        val hasWon = intent?.extras?.getBoolean("Won")
        val region = intent.extras!!.getString("Region")
        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/$region/$Name/$ID?size=10"

        // show progress bar while loading
        val progressBar = ProgressBar(this)
        progressBar.visibility = View.VISIBLE
        progressBar.isIndeterminate = true


//        if (hasWon == true) {
//            // insert winning sound here
//        } else {
//            // insert losing sound here
//        }


        doAsync {
            val matchID: String = if (IDofMatch == "none") {
                val jsonMatches = Henrik(this@MatchHistoryActivity).henrikAPI(allmatches)
                val data = jsonMatches["data"] as JSONArray
                val easier = data.getJSONObject(MatchNumber).getJSONObject("metadata")
                easier.getString("matchid")
            } else {
                IDofMatch!!
            }
            val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"
            matchJSON = Henrik(this@MatchHistoryActivity).henrikAPI(matchURl)
            val metadata = matchJSON.getJSONObject("data").getJSONObject("metadata")
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
                progressBar.visibility = View.GONE
                tabLayout =
                    findViewById<View>(id.tabsforMatch) as? TabLayout
                viewPager =
                    findViewById<View>(id.view_pager_matchHistory) as? ViewPager
                tabLayout?.tabGravity = TabLayout.GRAVITY_FILL
                tabLayout?.tabMode = TabLayout.MODE_AUTO

                tabLayout?.newTab()?.setText(getString(R.string.s107))
                    ?.setIcon(drawable.live).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                tabLayout?.newTab()?.setText(getString(R.string.s108))
                    ?.setIcon(drawable.details).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }
                tabLayout?.newTab()?.setText(getString(R.string.s109))
                    ?.setIcon(drawable.buddiesicon).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }
                tabLayout?.newTab()?.setText(getString(R.string.s110))
                    ?.setIcon(drawable.rounds_icno).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                tabLayout?.newTab()?.setText(getString(R.string.s111))
                    ?.setIcon(drawable.spikeicon).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                tabLayout?.newTab()?.setText(getString(R.string.s112))
                    ?.setIcon(drawable.killmapicon).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                tabLayout?.newTab()?.setText(getString(R.string.s113))
                    ?.setIcon(drawable.killicon).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                val adapter =
                    tabLayout?.tabCount?.let { MatchHistoryAdapter(supportFragmentManager, it) }
                viewPager?.adapter = adapter
                viewPager?.offscreenPageLimit = 10
                viewPager?.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabLayout))
                viewPager?.setPageTransformer(true, ZoomOutPageTransformer())
                tabLayout?.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                    override fun onTabSelected(tab: TabLayout.Tab) {
                        viewPager?.currentItem = tab.position
                        if (tab.position == 6 || tab.position == 2) {
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

                val title: TextView = findViewById(id.title)
                val matchData = matchJSON.get("data") as JSONObject
                val teams = matchData.getJSONObject("teams")
                var didredWin = false
                try {
                    didredWin = teams.getJSONObject("red").getBoolean("has_won")
                } catch (_: Exception) {
                }
                if (didredWin) {
                    val score = teams.getJSONObject("red").getString("rounds_won")
                    val lost: String = teams.getJSONObject("red").getString("rounds_lost")
                    title.text = "$map $score - $lost"
                } else {
                    val score = teams.getJSONObject("blue").getString("rounds_won")
                    val lost = teams.getJSONObject("blue").getString("rounds_lost")
                    title.text = "$map $score - $lost"
                }

                refresFab.setOnClickListener {
                    finish()
                }
            }
        }
    }

    companion object {
        lateinit var matchJSON: JSONObject
        var mapURL: String? = null
    }
}
