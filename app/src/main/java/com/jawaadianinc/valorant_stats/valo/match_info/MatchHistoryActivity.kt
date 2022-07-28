package com.jawaadianinc.valorant_stats.valo.match_info

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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


        if (hasWon == true) {
            // insert winning sound here
        } else {
            // insert losing sound here
        }


        doAsync {
            val matchID: String = if (IDofMatch == "none") {
                val jsonMatches = HenrikAPI(allmatches)
                val data = jsonMatches["data"] as JSONArray
                val easier = data.getJSONObject(MatchNumber).getJSONObject("metadata")
                easier.getString("matchid")
            } else {
                IDofMatch!!
            }
            val matchURl = "https://api.henrikdev.xyz/valorant/v2/match/$matchID"
            matchJSON = HenrikAPI(matchURl)
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
                tabLayout =
                    findViewById<View>(com.jawaadianinc.valorant_stats.R.id.tabsforMatch) as? TabLayout
                viewPager =
                    findViewById<View>(com.jawaadianinc.valorant_stats.R.id.view_pager_matchHistory) as? ViewPager
                tabLayout?.tabGravity = TabLayout.GRAVITY_CENTER
                tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
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

                val title: TextView = findViewById(com.jawaadianinc.valorant_stats.R.id.title)
                val matchData = matchJSON!!.get("data") as JSONObject
                val teams = matchData.getJSONObject("teams")
                val metadata = matchData.getJSONObject("metadata")
                val map = metadata.getString("map")
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
        var matchJSON: JSONObject? = null
        var mapURL: String? = null
    }

    private fun HenrikAPI(playerURL: String): JSONObject {
        val database = Firebase.database
        val keyRef = database.getReference("VALORANT/henrik")
        val json: JSONObject

        var henrik = ""

        try {
            keyRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    henrik = (dataSnapshot.value as String?).toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        } catch (e: Exception) {
            Log.d("Henrik", "Error: $e")
            //return JSONObject()
        } finally {
            val client = OkHttpClient()
            val urlBuilder: HttpUrl.Builder =
                playerURL.toHttpUrlOrNull()!!.newBuilder()
            val url = urlBuilder.build().toString()

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", henrik)
                .build()
            val call = client.newCall(request).execute()
            val response = call.body.string()
            json = JSONObject(response)
        }

        //Log.d("Henrik-Key", "Response: $henrik")

        return json
    }
}
