package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.SkuDetailsParams
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.main.AboutActivity
import com.jawaadianinc.valorant_stats.valo.activities.LoggingInActivityRSO
import com.jawaadianinc.valorant_stats.valo.activities.chat.ChatsForumActivity
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation

class SettingsFragment : Fragment() {
    lateinit var playerName: String
    lateinit var region: String

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

        val logOutButton = view.findViewById<View>(R.id.new_SignOutButton)
        logOutButton.setOnClickListener {
            // Add a confirmation dialog
            val alert = android.app.AlertDialog.Builder(requireActivity())
            alert.setTitle("Log out")
            alert.setMessage("Are you sure you want to log out?")
            alert.setPositiveButton("Yes") { _, _ ->
                logOut(playerName.split("#")[0])
            }
            alert.setNegativeButton("No") { _, _ -> }
            alert.show()
        }

        setupLanguage()

        // Get the player image
        val pfp = view.findViewById<ImageView>(R.id.Extras_PlayerPFP)
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
            val intent = Intent(requireActivity(), AboutActivity::class.java)
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
                    "Thank you for your donation!",
                    Toast.LENGTH_SHORT
                ).show()

            } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
                // Handle user cancellation
                Toast.makeText(requireActivity(), "Donation cancelled", Toast.LENGTH_SHORT).show()
            } else {
                // Handle other billing errors
                Toast.makeText(requireActivity(), "Donation failed", Toast.LENGTH_SHORT).show()
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

        val donateButton = view.findViewById<Button>(R.id.new_Donate)
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
            Toast.makeText(requireActivity(), "Coming soon!", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupLanguage()
    {
        val languageSpinner = view?.findViewById<Spinner>(R.id.language_selector)
        val languageCodes = arrayOf(
            "ar-AE",
            "de-DE",
            "en-US",
            "es-ES",
            "es-MX",
            "fr-FR",
            "id-ID",
            "it-IT",
            "ja-JP",
            "ko-KR",
            "pl-PL",
            "pt-BR",
            "ru-RU",
            "th-TH",
            "tr-TR",
            "vi-VN",
            "zh-CN",
            "zh-TW"
        )

        val languageNames = arrayOf(
            "Arabic",
            "German",
            "English",
            "Spanish (Spain)",
            "Spanish (Mexico)",
            "French",
            "Indonesian",
            "Italian",
            "Japanese",
            "Korean",
            "Polish",
            "Portuguese (Brazil)",
            "Russian",
            "Thai",
            "Turkish",
            "Vietnamese",
            "Chinese (Simplified)",
            "Chinese (Traditional)"
        )

        val languageAdapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            languageNames
        )

        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        languageSpinner?.adapter = languageAdapter

        val sharedPref = requireActivity().getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
        val locale = sharedPref.getString("locale", "en-US")
        if (locale in languageCodes) {
            val localeIndex = languageCodes.indexOf(locale)
            languageSpinner?.setSelection(localeIndex)
        } else {
            val localeIndex = languageCodes.indexOf("en-US")
            languageSpinner?.setSelection(localeIndex)
        }

        languageSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val language = languageCodes[position]
                val sharedPref = requireActivity().getSharedPreferences("UserLocale", Context.MODE_PRIVATE)
                val editor = sharedPref.edit()
                editor.putString("locale", language)
                editor.apply()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Another interface callback
            }
        }
    }

    private fun logOut(name: String) {
        val playerDB = PlayerDatabase(requireActivity())
        if (playerDB.logOutPlayer(name)) {
            val widgetIntent = Intent(requireActivity(), LastMatchWidget::class.java)
            widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
            val ids = AppWidgetManager.getInstance(activity?.application).getAppWidgetIds(
                ComponentName(activity?.applicationContext!!, LastMatchWidget::class.java)
            )
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
            activity?.sendBroadcast(widgetIntent)

            startActivity(Intent(requireActivity(), LoggingInActivityRSO::class.java))
            activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            activity?.finish()
            Toast.makeText(requireActivity(), "Logged out!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireActivity(), "Error logging out O_o", Toast.LENGTH_SHORT).show()
        }
    }


}
