package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class brawlStatsActivity : AppCompatActivity() {

    var tabLayout: TabLayout? = null
    var viewPager: ViewPager? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brawl_stats)
        val database = Firebase.database
        val playersRef = database.getReference("Brawlhalla/players")
        val brawlID = intent.extras!!.getString("BrawlID")
        val title: TextView = findViewById(R.id.title)
        title.text = "Fetching stats..."

        doAsync {
            try {
                val playerStats = URL + "/player/$brawlID/stats?api_key=${APIcode}"
                playerStatsJSON = JSONObject(URL(playerStats).readText())

                val playerRanked = URL + "/player/$brawlID/ranked?api_key=${APIcode}"
                playerRankedJson = JSONObject(URL(playerRanked).readText())

                try {
                    val ClanID: String =
                        JSONObject(URL(playerStats).readText()).getJSONObject("clan")
                            .getString("clan_id")
                    val playerClan = URL + "/clan/$ClanID/?api_key=${APIcode}"
                    Log.d("brawl", playerClan)
                    playerClanJSON = JSONObject(URL(playerClan).readText())
                } catch (e: Exception) {
                    //TODO handle player not in a existing clan
                }
            } catch (e: Exception) {
                uiThread {
                    AlertDialog.Builder(applicationContext).setTitle("ID Error!")
                        .setMessage("This ID does not exist! Try another name")
                        .setPositiveButton(android.R.string.ok) { _, _ ->
                            startActivity(Intent(applicationContext, brawlFindAccount::class.java))
                        }
                        .setIcon(android.R.drawable.ic_dialog_alert).show()
                }
            }
            uiThread {
                try {
                    title.text = (playerStatsJSON!!.getString("name")) + " ($brawlID)"
                    playersRef.child(brawlID!!).setValue(playerStatsJSON!!.getString("name"))
                } catch (e: Exception) {
                    Toast.makeText(applicationContext, "User doesn't exist", Toast.LENGTH_LONG)
                        .show()
                    startActivity(Intent(applicationContext, brawlFindAccount::class.java))
                }

                findViewById<ProgressBar>(R.id.progressBar2).visibility = View.INVISIBLE
                tabLayout = findViewById<View>(R.id.tabs) as? TabLayout
                viewPager = findViewById<View>(R.id.view_pager) as? ViewPager
                tabLayout?.tabGravity = TabLayout.GRAVITY_FILL
                tabLayout?.tabMode = TabLayout.MODE_SCROLLABLE
                tabLayout?.newTab()?.setText("Stats")?.setIcon(R.drawable.rounds_icno).let {
                    if (it != null) {
                        tabLayout?.addTab(it)
                    }
                }
                tabLayout?.newTab()?.setText("Ranked")?.setIcon(R.drawable.rankicon).let {
                    if (it != null) {
                        tabLayout?.addTab(it)
                    }
                }
                tabLayout?.newTab()?.setText("2v2 Ranked")?.setIcon(R.drawable.buddiesicon).let {
                    if (it != null) {
                        tabLayout?.addTab(it)
                    }
                }

                tabLayout?.newTab()?.setText("Clan")?.setIcon(R.drawable.players_icon).let {
                    if (it != null) {
                        tabLayout?.addTab(it)
                    }
                }

                val adapter = tabLayout?.tabCount?.let { BrawlAdapter(supportFragmentManager, it) }
                viewPager?.adapter = adapter
                viewPager?.offscreenPageLimit = 5
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
            }
        }
    }

    companion object {
        const val APIcode = "AK4GDM78QDQH4NFR4AZI"
        const val URL = "https://api.brawlhalla.com"
        var playerStatsJSON: JSONObject? = null
        var playerRankedJson: JSONObject? = null
        var playerClanJSON: JSONObject? = null
    }
}
