package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityStaticsMainBinding
import com.jawaadianinc.valorant_stats.main.LoadingActivity
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

        val updateDescription: String =
            "- Night market discounted prices are now shown with original prices crossed out" +
                    "\n- Fixed missing gun buddies & player titles in accessory store" +
                    "\n- All store timers now show days remaining" +
                    "\n- Fixed top and bottom spray being the wrong way around" +
                    "\n- A few more additions to Material You with certain icons tinting, default is also now blue" +
                    "\n- Fixed clipping issues with some parts of Statics"

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

        val statsFragment = StaticsMainMenu()
        val liveStatsFragment = LiveStatsFragment()
        val assetsFragment = AssetsFragment()
        activeFragment = liveStatsFragment

        val fragmentManager: FragmentManager = supportFragmentManager
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }

        supportFragmentManager.beginTransaction().apply {
            add(R.id.container, statsFragment, "1").hide(statsFragment)
            add(R.id.container, liveStatsFragment, "2")
            add(R.id.container, assetsFragment, "3").hide(assetsFragment)
        }.commit()


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

        toolbar.title = playerName
        Picasso.get().load(playerCardSmall).into(toolbarPicture)

        toolbarPicture.setOnClickListener {
            val inflater = this.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val popupView = inflater.inflate(R.layout.layout_statics_main_menu_pfp, null)
            val width = LinearLayout.LayoutParams.MATCH_PARENT
            val height = LinearLayout.LayoutParams.MATCH_PARENT
            val focusable = true
            val popupWindow = PopupWindow(popupView, width, height, focusable)
            popupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0)

            // animate the popupview in by coming from the top
            popupView.translationY = -1000f
            popupView.animate().translationY(0f).setDuration(500)
                .setInterpolator {
                    val x = it - 1.0f
                    x * x * x * x * x + 1.0f
                }
                .start()

            val dimLayout = findViewById<LinearLayout>(R.id.dim_layout)

            // set the dim layout to alpha 1f in 500ms
            dimLayout.animate().alpha(0.5f).setDuration(500).start()

            // set the popup window to dismiss when the back button is pressed
            popupWindow.setOnDismissListener {
                dimLayout.animate().alpha(0f).setDuration(500).start()
                popupView.animate().alpha(0f).setDuration(500).withEndAction {
                    popupWindow.dismiss()
                }.start()
            }

            popupView.alpha = 0f
            popupView.animate().alpha(1f).setDuration(500).start()

            val logout = popupView.findViewById<Button>(R.id.popup_LogOutButton)
            logout.setOnClickListener {
                // Add a confirmation dialog
                val alert = MaterialAlertDialogBuilder(this, R.style.AlertDialogTheme)
                alert.setTitle(getString(R.string.s55))
                alert.setMessage(getString(R.string.s56))
                alert.setPositiveButton("Yes") { _, _ ->
                    logOut()
                }
                alert.setNegativeButton("No") { _, _ -> }
                alert.show()
            }

            val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    // Handle successful purchase
                    // Process the donation and update UI accordingly
                    Toast.makeText(
                        this,
                        getString(R.string.s162),
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle user cancellation
                    Toast.makeText(this, getString(R.string.s163), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Handle other billing errors
                    Toast.makeText(this, getString(R.string.s164), Toast.LENGTH_SHORT)
                        .show()
                }
            }


            val billingClient = BillingClient.newBuilder(this)
                .setListener(purchaseUpdateListener)
                .enablePendingPurchases()
                .build()

            billingClient.startConnection(object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                        // The BillingClient is ready. You can query purchases here.
                    }
                }

                override fun onBillingServiceDisconnected() {
                    // Try to restart the connection on the next request to
                    // Google Play by calling the startConnection() method.
                }
            })

            val donate = popupView.findViewById<Button>(R.id.popup_donate)
            donate.setOnClickListener {
                val skuList = ArrayList<String>()
                skuList.add("statics_donations")
                val params = SkuDetailsParams.newBuilder()
                params.setSkusList(skuList).setType(BillingClient.SkuType.INAPP)
                billingClient.querySkuDetailsAsync(params.build()) { billingResult, skuDetailsList ->
                    if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && skuDetailsList != null) {
                        for (skuDetails in skuDetailsList) {
                            val flowParams = BillingFlowParams.newBuilder()
                                .setSkuDetails(skuDetails)
                                .build()
                            billingClient.launchBillingFlow(this, flowParams)
                        }
                    }
                }
            }

            //val wallpaper = popupView.findViewById<ImageView>(R.id.PopUp_LargePlayerPFP)
            //Picasso.get().load(playerCardLarge).into(wallpaper)

            val leaderBoardsBtn = popupView.findViewById<Button>(R.id.new_Leaderboards)
            leaderBoardsBtn.setOnClickListener {
                val intent = Intent(this, LeaderboardsV2::class.java)
                intent.putExtra("playerName", playerName)
                intent.putExtra("region", region)
                intent.putExtra("playerImage", StaticsMainActivity.playerCardID)
                startActivity(intent)
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }

            val aboutButton = popupView.findViewById<Button>(R.id.new_About)
            aboutButton.setOnClickListener {
                val intent = Intent(this, NewAbout::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }

            val resetLanguageButton = popupView.findViewById<Button>(R.id.pfp_reset_language)
            resetLanguageButton.setOnClickListener {
                val intent = Intent(this, RecentMatchesList::class.java)
                intent.putExtra("region", region)
                startActivity(intent)
//                val alert = android.app.AlertDialog.Builder(requireActivity())
//                alert.setTitle("Reset Language")
//                alert.setMessage("Are you sure you want to reset the language?")
//                alert.setPositiveButton("Yes") { _, _ ->
//                    val prefs =
//                        requireActivity().getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
//                    val editor = prefs.edit()
//                    editor.putString("locale", "")
//                    editor.apply()
//                    requireActivity().moveTaskToBack(true)
//                    Handler().postDelayed({
//                        // Bring the app back to the foreground
//                        val intent = Intent(requireActivity(), SplashActivity::class.java)
//                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                        startActivity(intent)
//                    }, 250)
//
//                }
//                alert.setNegativeButton("No") { _, _ -> }
//                alert.show()
            }

            val darkModeSwitch : MaterialSwitch = popupView.findViewById(R.id.switch_darkMode)
            // Get the current system night mode
            val currentNightMode = AppCompatDelegate.getDefaultNightMode()
            darkModeSwitch.isChecked = currentNightMode == AppCompatDelegate.MODE_NIGHT_YES
            darkModeSwitch.setOnCheckedChangeListener { _, isChecked ->
                val nightMode =
                    if (isChecked) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
                AppCompatDelegate.setDefaultNightMode(nightMode)
                finish()
                val intent = Intent(this, LoadingActivity::class.java)
                startActivity(intent)
//                // Finish the current activity
//                // Finish the current activity
//                finish()
//
//                // Start a new activity after the theme has been applied
//
//                // Start a new activity after the theme has been applied
//                val intent = Intent(this@CurrentActivity, NewActivity::class.java)
//                startActivity(intent)
            }
        }
    }

    fun requireActivity(): Activity {
        return this
    }

    private fun logOut() {
        val intent = Intent(this, NewLogInUI::class.java)
        intent.putExtra("login", "true")
        startActivity(intent)
        overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        finish()
    }

    private fun changeFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        fragmentManager.beginTransaction().apply {
            setCustomAnimations(R.anim.bounce_in, 0);
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
