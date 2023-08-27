package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityStaticsMainBinding
import com.jawaadianinc.valorant_stats.main.ValorantAccountDatabase
import com.jawaadianinc.valorant_stats.valo.activities.chat.ChatsForumActivity
import com.squareup.picasso.Picasso


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

    private lateinit var toolbar: MaterialToolbar
    private lateinit var toolbarPicture: ImageView

    val statsFragment = StaticsMainMenu()
    val liveStatsFragment = LiveStatsFragment()
    val assetsFragment = AssetsFragment()
    val chatsFragment = ChatsForumActivity()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        DynamicColors.applyToActivitiesIfAvailable(application)

        bottomNavBar = binding.bottomNavigationView
        toolbar = binding.materialToolbar3
        toolbarPicture = binding.staticsToolbarImage

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

        val accountsDB = ValorantAccountDatabase(this)
        if (!accountsDB.doesPlayerExist(puuid)) {
            if (accountsDB.addPlayer(playerName, cookies, puuid, playerImageID)) {
                Toast.makeText(this, "Added account!", Toast.LENGTH_SHORT).show()
            }
        }

        val updateDescription: String =
            "- Multi account support! :D" +
                    "\n- Statics will follow your current system theme for new users" +
                    "\n- Fixed a bug of agents being locked apart from the default ones" +
                    "\n- Fixed a crash for a new user account who hasn't set any sprays"

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
                val dialog = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                dialog.setTitle("${getString(R.string.s16)} ($versionName)")
                dialog.setMessage("${getString(R.string.s17)} \n\n$updateDescription")
                dialog.setPositiveButton("Ok") { dialog, which -> }
                dialog.show()
                with(sharedPref.edit()) {
                    putBoolean(versionName, true)
                    apply()
                }
            }
        } else {
            // check if they have seen the dialog that says that this is a beta version
            val sharedPref = getSharedPreferences("release", Context.MODE_PRIVATE)
            // make hasSeen based off the version name
            val hasSeen = sharedPref.getBoolean(versionName, false)
            if (!hasSeen) {
                // show the dialog
                val dialog = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
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

        activeFragment = liveStatsFragment

        bottomNavBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.new_Stats -> {
                    changeFragment(statsFragment)
                    true
                }

                R.id.new_Live -> {
                    changeFragment(liveStatsFragment)
                    true
                }

                R.id.new_Assets -> {
                    changeFragment(assetsFragment)
                    true
                }

                R.id.new_Chats -> {
                    changeFragment(chatsFragment)
                    true
                }

                else -> {
                    false
                }
            }
        }
        toolbar.title = playerName
        Picasso.get().load(playerCardSmall).into(toolbarPicture)

        toolbarPicture.setOnClickListener {
            requireActivity().startActivity(Intent(requireActivity(), SettingsActivity::class.java))
        }
    }

    override fun onStart() {
        super.onStart()
        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, statsFragment, "1").hide(statsFragment)
            add(R.id.container, liveStatsFragment, "2")
            add(R.id.container, assetsFragment, "3").hide(assetsFragment)
            add(R.id.container, chatsFragment, "4").hide(chatsFragment)
        }.commitAllowingStateLoss()
        bottomNavBar.selectedItemId = R.id.new_Live
    }

    override fun onPause() {
        super.onPause()
        val stats = supportFragmentManager.findFragmentByTag("1")
        val live = supportFragmentManager.findFragmentByTag("2")
        val assets = supportFragmentManager.findFragmentByTag("3")
        val chats = supportFragmentManager.findFragmentByTag("4")
        supportFragmentManager.beginTransaction().apply {
            if (stats != null) remove(stats)
            if (live != null) remove(live)
            if (assets != null) remove(assets)
            if (chats != null) remove(chats)
        }.commitAllowingStateLoss()
    }

    fun requireActivity(): Activity {
        return this
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().apply {
            hide(activeFragment)
            show(fragment)
        }.commit()
        activeFragment = fragment
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (activeFragment is LiveStatsFragment) {
            super.onBackPressed()
        } else {
            changeFragment(LiveStatsFragment())
            bottomNavBar.selectedItemId = R.id.new_Live
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
