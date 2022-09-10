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
import com.jawaadianinc.valorant_stats.main.ZoomOutPageTransformer
import com.jawaadianinc.valorant_stats.valo.adapters.MatchHistoryAdapter
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
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
        setContentView(com.jawaadianinc.valorant_stats.R.layout.activity_match_history)

        val refresFab: FloatingActionButton =
            findViewById(com.jawaadianinc.valorant_stats.R.id.refreshFab2)
        val Name = intent.extras!!.getString("RiotName")
        val ID = intent.extras!!.getString("RiotID")
        val MatchNumber = intent.extras!!.getInt("MatchNumber")
        val IDofMatch = intent.extras!!.getString("MatchID")
        val hasWon = intent?.extras?.getBoolean("Won")
        val allmatches = "https://api.henrikdev.xyz/valorant/v3/matches/eu/$Name/$ID?size=10"

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
                val jsonMatches = henrikAPI(allmatches)
                val data = jsonMatches["data"] as JSONArray
                val easier = data.getJSONObject(MatchNumber).getJSONObject("metadata")
                easier.getString("matchid")
            } else {
                IDofMatch!!
            }
            val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"
            matchJSON = henrikAPI(matchURl)
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
                    findViewById<View>(com.jawaadianinc.valorant_stats.R.id.tabsforMatch) as? TabLayout
                viewPager =
                    findViewById<View>(com.jawaadianinc.valorant_stats.R.id.view_pager_matchHistory) as? ViewPager
                tabLayout?.tabGravity = TabLayout.GRAVITY_FILL
                tabLayout?.tabMode = TabLayout.MODE_AUTO

                tabLayout?.newTab()?.setText("Overview")
                    ?.setIcon(com.jawaadianinc.valorant_stats.R.drawable.live).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                tabLayout?.newTab()?.setText("Details")
                    ?.setIcon(com.jawaadianinc.valorant_stats.R.drawable.details).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }
                tabLayout?.newTab()?.setText("Players")
                    ?.setIcon(com.jawaadianinc.valorant_stats.R.drawable.players_icon).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }
                tabLayout?.newTab()?.setText("Rounds")
                    ?.setIcon(com.jawaadianinc.valorant_stats.R.drawable.rounds_icno).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                tabLayout?.newTab()?.setText("Spike Map")
                    ?.setIcon(com.jawaadianinc.valorant_stats.R.drawable.spikeicon).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                tabLayout?.newTab()?.setText("Kill Map")
                    ?.setIcon(com.jawaadianinc.valorant_stats.R.drawable.killmapicon).let {
                        if (it != null) {
                            tabLayout?.addTab(it)
                        }
                    }

                tabLayout?.newTab()?.setText("Kill Feed")
                    ?.setIcon(com.jawaadianinc.valorant_stats.R.drawable.killicon).let {
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

                val title: TextView = findViewById(com.jawaadianinc.valorant_stats.R.id.title)
                val matchData = matchJSON.get("data") as JSONObject
                val teams = matchData.getJSONObject("teams")
                var didredWin = false
                try {
                    didredWin = teams.getJSONObject("red").getBoolean("has_won")
                } catch (e: Exception) {
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

    private fun henrikAPI(playerURL: String): JSONObject {
        return executeRequest(playerURL)
    }

    private fun executeRequest(playerURL: String): JSONObject {
        val client = OkHttpClient()
        val urlBuilder: HttpUrl.Builder =
            playerURL.toHttpUrlOrNull()!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "HDEV-67e86af9-8bf9-4f6d-b628-f4521b20d772")
            .build()
        return JSONObject(client.newCall(request).execute().body.string())
    }
}
