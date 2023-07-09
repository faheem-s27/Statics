package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.color.DynamicColors
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityStaticsMainBinding
import com.jawaadianinc.valorant_stats.main.SplashActivity
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
                "- Only agents that you have unlocked will be selectable in agent select" +
                        "- New updated translations and hopefully faster translations for the future!"

        DynamicColors.applyToActivitiesIfAvailable(application)

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
            val width = LinearLayout.LayoutParams.WRAP_CONTENT
            val height = LinearLayout.LayoutParams.WRAP_CONTENT
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
            dimLayout.animate().alpha(1f).setDuration(500).start()

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
                val alert = android.app.AlertDialog.Builder(this, R.style.AlertDialogTheme)
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
                val alert = android.app.AlertDialog.Builder(requireActivity())
                alert.setTitle("Reset Language")
                alert.setMessage("Are you sure you want to reset the language?")
                alert.setPositiveButton("Yes") { _, _ ->
                    val prefs =
                        requireActivity().getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
                    val editor = prefs.edit()
                    editor.putString("locale", "")
                    editor.apply()
                    requireActivity().moveTaskToBack(true)
                    Handler().postDelayed({
                        // Bring the app back to the foreground
                        val intent = Intent(requireActivity(), SplashActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        startActivity(intent)
                    }, 250)

                }
                alert.setNegativeButton("No") { _, _ -> }
                alert.show()
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
