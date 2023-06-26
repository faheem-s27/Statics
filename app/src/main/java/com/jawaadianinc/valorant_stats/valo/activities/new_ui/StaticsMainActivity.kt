package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityStaticsMainBinding

class StaticsMainActivity : AppCompatActivity() {
    lateinit var playerName: String
    lateinit var region: String
    lateinit var key: String
    lateinit var playerImageID: String
    lateinit var puuid : String
    lateinit var accessToken : String
    lateinit var entitlementToken: String
    lateinit var cookies : String
    lateinit var build: String
    lateinit var clientVersion: String

    private lateinit var activeFragment: Fragment

    private lateinit var binding: ActivityStaticsMainBinding
    private lateinit var bottomNavBar: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavBar = binding.bottomNavigationView

        // get the player name from the previous activity
        playerName = intent.getStringExtra("playerName").toString()
        region = intent.getStringExtra("region").toString()
        key = intent.getStringExtra("key").toString()
        playerImageID = intent.getStringExtra("playerImageID").toString()
        puuid = intent.getStringExtra("puuid").toString()
        accessToken = intent.getStringExtra("accessToken").toString()
        entitlementToken = intent.getStringExtra("entitlement").toString()
        cookies = intent.getStringExtra("cookies").toString()
        build = intent.getStringExtra("build").toString()
        clientVersion = intent.getStringExtra("clientVersion").toString()

        val UserLocalePrefs = getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
        locale = UserLocalePrefs.getString("locale", "").toString()

        playerCardSmall = "https://media.valorant-api.com/playercards/$playerImageID/smallart.png"
        playerCardLarge = "https://media.valorant-api.com/playercards/$playerImageID/largeart.png"
        playerCardWide = "https://media.valorant-api.com/playercards/$playerImageID/wideart.png"
        playerCardID = playerImageID

        val updateDescription: String = "- You can now see live players ranks in all game modes at all times!" +
                "\n Including Deathmatch, Escalation, and Replication!"
        // put the update description in the shared preferences
        val update = getSharedPreferences("LatestFeature", Context.MODE_PRIVATE)
        with(update.edit()) {
            putString("LatestFeatureDescription", updateDescription)
            apply()
        }

        // check if the version name is beta or not
        val versionName = packageManager.getPackageInfo(packageName, 0).versionName
        if (versionName.contains("Beta")) {
            // check if they have seen the dialog that says that this is a beta version
            val sharedPref = getSharedPreferences("beta", Context.MODE_PRIVATE)
            // make hasSeen based off the version name
            val hasSeen = sharedPref.getBoolean(versionName, false)
            if (!hasSeen) {
                // show the dialog
                val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                dialog.setTitle("${getString(R.string.s16)} ($versionName)")
                dialog.setMessage("${getString(R.string.s17)} \n\n$updateDescription")
                dialog.setPositiveButton("Ok") { _, _ -> }
                dialog.show()
                with(sharedPref.edit()) {
                    putBoolean(versionName, true)
                    apply()
                }
            }
        } else if (versionName.contains("Release")) {
            // check if they have seen the dialog that says that this is a beta version
            val sharedPref = getSharedPreferences("release", Context.MODE_PRIVATE)
            // make hasSeen based off the version name
            val hasSeen = sharedPref.getBoolean(versionName, false)
            if (!hasSeen) {
                // show the dialog
                val dialog = AlertDialog.Builder(this, R.style.AlertDialogTheme)
                dialog.setTitle("${getString(R.string.s18)} ($versionName)")
                dialog.setMessage("${getString(R.string.s19)} \n\n$updateDescription")
                dialog.setPositiveButton("Ok") { _, _ -> }
                dialog.show()
                with(sharedPref.edit()) {
                    putBoolean(versionName, true)
                    apply()
                }
            }
        }

        val statsFragment = StaticsMainMenu()
        val LiveStatsFragment = LiveStatsFragment()
        val AssetsFragment = AssetsFragment()
        val SettingsFragment = SettingsFragment()
        val fragmentManager = supportFragmentManager
        activeFragment = LiveStatsFragment

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                // Request the update.
                val MY_REQUEST_CODE = 0
                appUpdateManager.startUpdateFlowForResult(
                    appUpdateInfo,
                    AppUpdateType.IMMEDIATE,
                    this,
                    MY_REQUEST_CODE
                )
            }
        }

        fragmentManager.beginTransaction().apply {
            add(R.id.container, AssetsFragment, "1").hide(AssetsFragment)
            add(R.id.container, LiveStatsFragment, "2")
            add(R.id.container, statsFragment, "3").hide(statsFragment)
            add(R.id.container, SettingsFragment, "4").hide(SettingsFragment)
        }.commit()

        bottomNavBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.new_Stats -> {
                    changeFragment(statsFragment)
                    true
                }
                R.id.new_Live -> {
                    changeFragment(LiveStatsFragment)
                    true
                }
                R.id.new_Assets -> {
                    changeFragment(AssetsFragment)
                    true
                }
                R.id.new_Settings -> {
                    changeFragment(SettingsFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }

        // update the widget
        val intent =
            Intent(this, LastMatchWidget::class.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(applicationContext, LastMatchWidget::class.java)
        )
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(intent)

    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().apply {
            setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
            hide(activeFragment)
            show(fragment)
        }.commit()
        activeFragment = fragment
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (activeFragment is StaticsMainMenu) {
            super.onBackPressed()
        } else {
            changeFragment(StaticsMainMenu())
            bottomNavBar.selectedItemId = R.id.new_Stats
        }
    }

    companion object {
        var playerCardWide = ""
        var playerCardLarge = ""
        var playerCardSmall = ""
        var playerCardID = ""
        var locale = ""
    }
}
