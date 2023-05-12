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

        playerCardSmall = "https://media.valorant-api.com/playercards/$playerImageID/smallart.png"
        playerCardLarge = "https://media.valorant-api.com/playercards/$playerImageID/largeart.png"
        playerCardWide = "https://media.valorant-api.com/playercards/$playerImageID/wideart.png"
        playerCardID = playerImageID

        // check if userlocale is stored in shared preferences
        val sharedPref = getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
        val storedLocale = sharedPref.getString("locale", "")
        if (storedLocale == "") {
            // update the user locale
            val locale = resources.configuration.locales.get(0)
            val language = "${locale.language}-${locale.country}"
            with(sharedPref.edit()) {
                putString("locale", language)
                apply()
            }
        }

        // get the value from shared preferences
        UserLocale = sharedPref.getString("locale", "")!!
        //Toast.makeText(this, "Device language: $UserLocale", Toast.LENGTH_SHORT).show()

        val updateDescription =
            "- Testing languages in: - French" +
                    "\n- Portuguese" +
                    "\n- Portuguese (Brazil)" +
                    "\n- Russian" +
                    "\nThey may not be accurate so please let Dev Duckie know when that happens 🦆❤️" +
                    "\n\n- Fixed getting stuck on 'Checking connection'" +
                    "\n- Updated shop data visuals"
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
                dialog.setTitle("${getString(R.string.BetaDisclaimerTitle)} ($versionName)")
                dialog.setMessage("${getString(R.string.BetaDisclaimerDesc)} \n\n$updateDescription")
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
                dialog.setTitle("${getString(R.string.NewFeaturesForStatics)} ($versionName)")
                dialog.setMessage("${getString(R.string.NewFeaturesIncludes)} \n\n$updateDescription")
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
        val ESportsFragment = ESportsFragment()
        val SettingsFragment = SettingsFragment()
        val fragmentManager = supportFragmentManager
        activeFragment = statsFragment

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
            add(R.id.container, LiveStatsFragment, "2").hide(LiveStatsFragment)
            add(R.id.container, statsFragment, "3")
            add(R.id.container, ESportsFragment, "4").hide(ESportsFragment)
            add(R.id.container, SettingsFragment, "5").hide(SettingsFragment)
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

                R.id.new_Esports -> {
                    changeFragment(ESportsFragment)
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
        var UserLocale = ""
    }
}
