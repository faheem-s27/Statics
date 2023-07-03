package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.main.LoadingActivity
import com.jawaadianinc.valorant_stats.main.SplashActivity
import com.jawaadianinc.valorant_stats.valo.activities.chat.ChatsForumActivity
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import java.util.Locale

class SettingsFragment : Fragment() {
    lateinit var playerName: String
    lateinit var region: String
    var count = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_extras, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerName = activity?.intent?.getStringExtra("playerName").toString()
        region = activity?.intent?.getStringExtra("region").toString()

        val logOutButton = view.findViewById<View>(R.id.popup_LogOutButton)
        logOutButton.setOnClickListener {
            // Add a confirmation dialog
            val alert = android.app.AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
            alert.setTitle(getString(R.string.s55))
            alert.setMessage(getString(R.string.s56))
            alert.setPositiveButton("Yes") { _, _ ->
                logOut()
            }
            alert.setNegativeButton("No") { _, _ -> }
            alert.show()
        }

        val resetLanguageButton = view.findViewById<Button>(R.id.pfp_reset_language)
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
                // completely finish the app to go homescreen then launch it again
//                val intent = Intent(requireActivity(), SplashActivity::class.java)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
//                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
//                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                startActivity(intent)
//                activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
//                activity?.finish()

                // Move the app to the background
                requireActivity().moveTaskToBack(true)

// Wait for 2 seconds
                Handler().postDelayed({
                    // Bring the app back to the foreground
                    val intent = Intent(requireActivity(), SplashActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    startActivity(intent)
                }, 500)

            }
            alert.setNegativeButton("No") { _, _ -> }
            alert.show()
        }

        //setStaticsLanguage()

        // Get the player image
        val pfp = view.findViewById<ImageView>(R.id.agent_select_agent)
        val Lpfp = view.findViewById<ImageView>(R.id.Extras_LargePlayerPFP)
        // Set the player image
        Picasso.get().load(StaticsMainActivity.playerCardSmall).fit().centerCrop().into(pfp)
        Picasso.get().load(StaticsMainActivity.playerCardLarge).fit().centerCrop()
            .transform(BlurTransformation(requireContext())).into(Lpfp)

        val ChatsButton = view.findViewById<Button>(R.id.new_Chats)
        ChatsButton.setOnClickListener {
            val intent = Intent(requireActivity(), ChatsForumActivity::class.java)
            intent.putExtra("playerName", playerName)
            intent.putExtra("playerImage", StaticsMainActivity.playerCardID)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        val playerNameTV = view.findViewById<TextView>(R.id.Extras_PlayerName)
        // if playername is more than 14 characters, make the text smaller
        if (playerName.length > 14) {
            playerNameTV.textSize = 20f
        }
        playerNameTV.text = playerName

        val leaderBoardsBtn = view.findViewById<Button>(R.id.new_Leaderboards)
        leaderBoardsBtn.setOnClickListener {
            val intent = Intent(requireActivity(), LeaderboardsV2::class.java)
            intent.putExtra("playerName", playerName)
            intent.putExtra("region", region)
            intent.putExtra("playerImage", StaticsMainActivity.playerCardID)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

        val aboutButton = view.findViewById<Button>(R.id.new_About)
        aboutButton.setOnClickListener {
            val intent = Intent(requireActivity(), NewAbout::class.java)
            startActivity(intent)
            activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        }

//        // Implement a PurchaseUpdateListener to handle purchase updates
        val purchaseUpdateListener = PurchasesUpdatedListener { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
                // Handle successful purchase
                // Process the donation and update UI accordingly
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.s162),
                    Toast.LENGTH_SHORT
                ).show()

            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle user cancellation
                Toast.makeText(requireActivity(), getString(R.string.s163), Toast.LENGTH_SHORT)
                    .show()
            } else {
                // Handle other billing errors
                Toast.makeText(requireActivity(), getString(R.string.s164), Toast.LENGTH_SHORT)
                    .show()
            }
        }

        val billingClient = BillingClient.newBuilder(requireActivity())
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

        val donateButton = view.findViewById<Button>(R.id.popup_donate)
        donateButton.setOnClickListener {
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
                        billingClient.launchBillingFlow(requireActivity(), flowParams)
                    }
                }
            }
        }

        val logs = view.findViewById<Button>(R.id.new_view_logs_button)
        logs.setOnClickListener {
            //startActivity(Intent(requireActivity(), RequestLogActivity::class.java))
        }
    }

//    private fun setupLanguage()
//    {
//        val languageSpinner = view?.findViewById<Spinner>(R.id.language_selector)
//        val languageCodes = arrayOf(
//            "ar-AE",
//            "de-DE",
//            "en-US",
//            "es-ES",
//            "es-MX",
//            "fr-FR",
//            "id-ID",
//            "it-IT",
//            "ja-JP",
//            "ko-KR",
//            "pl-PL",
//            "pt-BR",
//            "ru-RU",
//            "th-TH",
//            "tr-TR",
//            "vi-VN",
//            "zh-CN",
//            "zh-TW"
//        )
//
//        val languageNames = arrayOf(
//            "Arabic",
//            "German",
//            "English",
//            "Spanish (Spain)",
//            "Spanish (Mexico)",
//            "French",
//            "Indonesian",
//            "Italian",
//            "Japanese",
//            "Korean",
//            "Polish",
//            "Portuguese (Brazil)",
//            "Russian",
//            "Thai",
//            "Turkish",
//            "Vietnamese",
//            "Chinese (Simplified)",
//            "Chinese (Traditional)"
//        )
//
//        val languageAdapter = ArrayAdapter(
//            requireActivity(),
//            android.R.layout.simple_spinner_item,
//            languageNames
//        )
//
//        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        languageSpinner?.adapter = languageAdapter
//
//        val sharedPref = requireActivity().getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
//        val locale = sharedPref.getString("locale", "en-US")
//        if (locale in languageCodes) {
//            val localeIndex = languageCodes.indexOf(locale)
//            languageSpinner?.setSelection(localeIndex)
//        } else {
//            val localeIndex = languageCodes.indexOf("en-US")
//            languageSpinner?.setSelection(localeIndex)
//        }
//
//        languageSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                val language = languageCodes[position]
//                val sharedPref = requireActivity().getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
//                val editor = sharedPref.edit()
//                editor.putString("locale", language)
//                editor.apply()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Another interface callback
//            }
//        }
//    }
//
//    private fun setStaticsLanguage()
//    {
//        val languageSpinner = view?.findViewById<Spinner>(R.id.language_selector)
//
//        // create a language array which has english, french, portuguese, portuguese brazil, russian and vietnamese
//        val languageCodes = arrayOf(
//            "en-US",
//            "fr-FR",
//            "pt-PT",
//            "pt-BR",
//            "ru-RU",
//            "vi-VN"
//        )
//
//        val languageNames = arrayOf(
//            "English",
//            "French",
//            "Portuguese",
//            "Portuguese (Brazil)",
//            "Russian",
//            "Vietnamese"
//        )
//
//        languageSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//            override fun onItemSelected(
//                parent: AdapterView<*>,
//                view: View?,
//                position: Int,
//                id: Long
//            ) {
//                val language = languageCodes[position]
//                if (count != 0) changeAppLanguage(language, requireActivity())
//                else count++
//                // restart activity
//                //requireActivity().recreate()
//            }
//
//            override fun onNothingSelected(parent: AdapterView<*>) {
//                // Another interface callback
//            }
//        }
//
//        val languageAdapter = ArrayAdapter(
//            requireActivity(),
//            android.R.layout.simple_spinner_item,
//            languageNames
//        )
//
//        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//        languageSpinner?.adapter = languageAdapter
//
//        // check if the user has already selected a language
//        val sharedPref = requireActivity().getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
//        val locale = sharedPref.getString("locale", "")
//        if (locale in languageCodes) {
//            val localeIndex = languageCodes.indexOf(locale)
//            languageSpinner?.setSelection(localeIndex)
//            //Toast.makeText(requireActivity(), "Language set to $locale", Toast.LENGTH_SHORT).show()
//        } else {
//            val localeIndex = languageCodes.indexOf("en-US")
//            languageSpinner?.setSelection(localeIndex)
//        }
//
//        // set language spinner to nothing
//        //languageSpinner?.setSelection(-1)
//    }

    fun changeAppLanguage(languageCode: String, context: Context) {
        val locale = if (languageCode.contains('-')) {
            val codes = languageCode.split('-')
            Locale(codes[0], codes[1])
        } else {
            Locale(languageCode)
        }
        Locale.setDefault(locale)
        val config = Configuration()
        config.setLocale(locale)
        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        val sharedPref = context.getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
        val editor = sharedPref.edit()
        editor.putString("locale", languageCode)
        editor.apply()

        Toast.makeText(context, "Language changed to $languageCode", Toast.LENGTH_SHORT).show()

        val intent = Intent(requireActivity(), LoadingActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

//    fun getAvailableLanguages(): List<String> {
//        val locales = Resources.getSystem().assets.locales
//        val languages = mutableListOf<String>()
//        for (locale in locales) {
//            val language = locale.language
//            if (!languages.contains(language) && language.isNotEmpty()) {
//                languages.add(language)
//            }
//        }
//        return languages
//    }


    private fun logOut() {
        val intent = Intent(requireActivity(), NewLogInUI::class.java)
        intent.putExtra("login", "true")
        startActivity(intent)
        activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
        activity?.finish()
    }
}
