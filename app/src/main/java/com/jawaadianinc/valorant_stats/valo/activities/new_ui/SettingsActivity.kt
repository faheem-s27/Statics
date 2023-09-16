package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import androidx.preference.SwitchPreference
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.main.AccountSelectionActivity
import com.jawaadianinc.valorant_stats.main.MyApplication


class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val logOutPreference: Preference? = findPreference("log_out")
            logOutPreference?.setOnPreferenceClickListener {
                // Add a confirmation dialog
                val alert = MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogTheme)
                alert.setTitle(getString(R.string.s55))
                alert.setMessage(getString(R.string.s56))
                alert.setPositiveButton("Yes") { _, _ ->
                    logOut()
                }
                alert.setNegativeButton("No") { _, _ -> }
                alert.show()
                true // Return true to indicate the click has been handled
            }

            val versionName = getAppVersionName()
            val currentVersionPreference: Preference? = findPreference("current_version")
            if (currentVersionPreference != null) {
                currentVersionPreference.summary = versionName
            }

            val from_play_storePreference = findPreference("from_play_store") as Preference?
            if (verifyInstallerId(requireActivity())) {
                from_play_storePreference?.summary = "Yes"
            } else {
                from_play_storePreference?.summary = "No"
            }

            val aboutStatics: Preference? = findPreference("about_statics")
            aboutStatics?.setOnPreferenceClickListener {
                val intent = Intent(requireActivity(), NewAbout::class.java)
                startActivity(intent)
                requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                true
            }

            val new_changes: Preference? = findPreference("latest_statics")
            new_changes?.setOnPreferenceClickListener {
                val sharedPref =
                    requireActivity().getSharedPreferences("LatestFeature", MODE_PRIVATE)
                val latestFeatureDescription =
                    sharedPref.getString("LatestFeatureDescription", "No description available")

                MaterialAlertDialogBuilder(requireActivity(), R.style.AlertDialogTheme).apply {
                    setTitle("What's new in $versionName")
                    setMessage(latestFeatureDescription)
                    setPositiveButton("Ok") { _, _ -> }
                    show()
                }
                true
            }


            val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                    // Handle successful purchase
                    // Process the donation and update UI accordingly
                    Toast.makeText(
                        thiss(),
                        getString(R.string.s162),
                        Toast.LENGTH_SHORT
                    ).show()

                } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                    // Handle user cancellation
                    Toast.makeText(thiss(), getString(R.string.s163), Toast.LENGTH_SHORT)
                        .show()
                } else {
                    // Handle other billing errors
                    Toast.makeText(thiss(), getString(R.string.s164), Toast.LENGTH_SHORT)
                        .show()
                }
            }


            val billingClient = BillingClient.newBuilder(thiss())
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

            val donate: Preference? = findPreference("donate")
            donate?.setOnPreferenceClickListener {
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
                            billingClient.launchBillingFlow(thiss(), flowParams)
                        }
                    }
                }
                true
            }

            val matchNotificationsPreference: SwitchPreference? =
                findPreference("match_notifications")
            matchNotificationsPreference?.setOnPreferenceChangeListener { _, newValue ->
                // Store the preference value in SharedPreferences
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                sharedPreferences.edit().putBoolean("match_notifications", newValue as Boolean)
                    .apply()
                true // Return true to indicate that the preference change should be saved
            }

            val chatNotifcationPreference: SwitchPreference? =
                findPreference("chat_notifications")
            chatNotifcationPreference?.setOnPreferenceChangeListener { _, newValue ->
                // Store the preference value in SharedPreferences
                val sharedPreferences =
                    PreferenceManager.getDefaultSharedPreferences(requireContext())
                sharedPreferences.edit().putBoolean("chat_notifications", newValue as Boolean)
                    .apply()
                true // Return true to indicate that the preference change should be saved
            }

            val darkModePreference: SwitchPreference? = findPreference("dark_mode")
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext())

            // Set the initial checked state based on user's dark mode preference
            darkModePreference?.isChecked = sharedPreferences.getBoolean("dark_mode", false)

            darkModePreference?.setOnPreferenceChangeListener { _, newValue ->
                if (newValue is Boolean) {
                    if (newValue) {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                    } else {
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                    }
                }
                true // Return true to indicate that the preference change should be saved
            }

            val languagePreference = findPreference<ListPreference>("language")
            languagePreference?.setOnPreferenceChangeListener { _, newValue ->
                // Update the app's language based on the selected value
                (requireActivity().application as MyApplication).updateLanguage(newValue as String)
                true
            }

        }

        private fun logOut() {
            val intent = Intent(requireActivity(), AccountSelectionActivity::class.java)
            startActivity(intent)
            requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            activity?.finish()
        }

        fun thiss(): Activity {
            return requireActivity()
        }

        private fun getAppVersionName(): String? {
            return try {
                val packageInfo = requireContext().packageManager
                    .getPackageInfo(requireContext().packageName, 0)
                packageInfo.versionName
            } catch (e: PackageManager.NameNotFoundException) {
                e.printStackTrace()
                "Unknown"
            }
        }

        fun verifyInstallerId(context: Context): Boolean {
            // A list with valid installers package name
            val validInstallers: List<String> =
                ArrayList(mutableListOf("com.android.vending", "com.google.android.feedback"))

            // The package name of the app that has installed your app
            val installer: String? =
                context.packageManager.getInstallerPackageName(context.packageName)

            // true if your app has been downloaded from Play Store
            return installer != null && validInstallers.contains(installer)
        }
    }


}
