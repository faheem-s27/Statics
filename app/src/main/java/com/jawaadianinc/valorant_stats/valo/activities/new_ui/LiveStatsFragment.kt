package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.ProgressDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.switchmaterial.SwitchMaterial
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.main.LoadingActivity
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.net.URL
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.util.Calendar
import java.util.Locale

data class ValorantPing(val ServerName: String, val PingValue: Int)

class LiveStatsFragment : Fragment() {
    lateinit var playerName: String
    lateinit var region: String
    lateinit var shard: String
    lateinit var authPreferences: SharedPreferences
    var accessToken: String? = null
    var entitlementToken: String? = null
    var PlayerUUID: String? = null
    lateinit var INITVIEW: View
    lateinit var LIVEVIEW: View
    lateinit var CurrentScreen: String
    lateinit var ClientVersion: String
    var client = OkHttpClient()
    private val clientPlatformToken =
        "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9"
    lateinit var gameModes: Array<String>
    lateinit var riotClientVersion: String
    var playerPartyID: String? = null
    var partyState: String? = null

    private var partyExpanded = true
    private var loadoutExpanded = true
    private var storeExpanded = true
    private var SpraysIDImage = HashMap<String, String>()
    private var AgentNamesID = HashMap<String, String>()
    private var MapsImagesID = HashMap<String, String>()
    private var GamePodStrings = HashMap<String, String>()

    private var notificationSent = false

    private var timerSeconds: Long = 500
    lateinit var assetsDB: AssetsDatabase

    private lateinit var agentPreGameRecyclerView: RecyclerView
    var storeTimer: CountDownTimer? = null

    private lateinit var weaponsJSONObject: JSONObject
    private lateinit var contenttierJSONObject: JSONObject
    private lateinit var weaponsSkinLevels: JSONObject
    private lateinit var titlesJSON: JSONObject

    private lateinit var loadingDialogStatics: androidx.appcompat.app.AlertDialog
    private lateinit var currentLoadoutWeaponsRecyclerView: RecyclerView

    private lateinit var RequestLogsDatabase: RequestLogsDatabase

    private lateinit var liveModeScope: CoroutineScope

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_live_stats, container, false)
    }

    private fun logLIVEStuff(log: String) {
        Log.d("LIVE_SETUP", log)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerName = activity?.intent?.getStringExtra("playerName") ?: return
        region = activity?.intent?.getStringExtra("region") ?: return
        authPreferences = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)

        loadingDialogStatics = ProgressDialogStatics().setProgressDialog(requireActivity(), "Getting live data")

        RequestLogsDatabase = RequestLogsDatabase(requireContext())

        liveModeScope = CoroutineScope(Dispatchers.IO)

        shard =
            if (region.lowercase(Locale.ROOT) == "latam" || region.lowercase(Locale.getDefault()) == "br") {
                "na"
            } else {
                region.lowercase(Locale.getDefault())
            }

        //loadingDialogStatics.show()

        INITVIEW = requireView().findViewById(R.id.InitView)
        LIVEVIEW = requireView().findViewById(R.id.LiveView)

        agentPreGameRecyclerView = requireView().findViewById(R.id.new_agentSelectGridRecyclerView)
        agentPreGameRecyclerView.layoutManager = GridLayoutManager(requireContext(), 6)

        currentLoadoutWeaponsRecyclerView = requireView().findViewById(R.id.currentLoadoutWeaponsRecyclerView)

        INITVIEW.visibility = View.VISIBLE
        LIVEVIEW.visibility = View.GONE

        val partyPlayerNameTV = requireView().findViewById<TextView>(R.id.new_partyPlayerName)
        val partyPlayerTagTV = requireView().findViewById<TextView>(R.id.new_partyPlayerTag)
        partyPlayerNameTV.text = playerName.split("#")[0]
        partyPlayerTagTV.text = "#"+playerName.split("#")[1]

        PlayerUUID = activity?.intent?.getStringExtra("puuid")
        accessToken = activity?.intent?.getStringExtra("accessToken")
        entitlementToken = activity?.intent?.getStringExtra("entitlement")

        // add the game modes to the array
        gameModes = arrayOf(
            "Unrated",
            "Competitive",
            "Swift Play",
            "Spike Rush",
            "Deathmatch",
            "Escalation",
            "Replication"
        )

        assetsDB = AssetsDatabase(requireContext())

        val progressDialog = requireActivity().findViewById(R.id.continueInit2) as ProgressBar
        progressDialog.max = 100
        progressDialog.progress = 0

        liveModeScope.launch {
            val url = "https://valorant-api.com/v1/version"
            val json = JSONObject(URL(url).readText())
            ClientVersion = json.getJSONObject("data").getString("riotClientBuild")
            riotClientVersion = json.getJSONObject("data").getString("riotClientVersion")

            logLIVEStuff("Got client version: $ClientVersion and riot client version: $riotClientVersion")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 10
            }

            val spraysURL = "https://valorant-api.com/v1/sprays"
            val spraysJSON = JSONObject(URL(spraysURL).readText())
            val spraysData = spraysJSON.getJSONArray("data")
            for (i in 0 until spraysData.length()) {
                val spray = spraysData.getJSONObject(i)
                SpraysIDImage[spray.getString("uuid")] = spray.getString("fullTransparentIcon")
            }

            logLIVEStuff("Got sprays")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 20
            }

            val agentsURL = "https://valorant-api.com/v1/agents?isPlayableCharacter=true"
            val agentsJSON = JSONObject(URL(agentsURL).readText())
            val agentsData = agentsJSON.getJSONArray("data")
            for (i in 0 until agentsData.length()) {
                val agent = agentsData.getJSONObject(i)
                AgentNamesID[agent.getString("uuid")] = agent.getString("displayName")
            }

            logLIVEStuff("Got agents")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 30
            }

            val mapsURL = "https://valorant-api.com/v1/maps"
            val mapsJSON = JSONObject(URL(mapsURL).readText())
            val mapsData = mapsJSON.getJSONArray("data")
            for (i in 0 until mapsData.length()) {
                val map = mapsData.getJSONObject(i)
                MapsImagesID[map.getString("mapUrl")] = map.getString("splash")
            }

            logLIVEStuff("Got maps")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 40
            }

            val gamePodURL = "https://valorant-api.com/internal/locres/en-US"
            val gamePodJSON = JSONObject(URL(gamePodURL).readText())
            val gamePodData = gamePodJSON.getJSONObject("data")
            val gamePodUIStrings = gamePodData.getJSONObject("UI_GamePodStrings")
            // for each object in the game pod strings get the key and value and add it to the hashmap
            for (key in gamePodUIStrings.keys()) {
                GamePodStrings[key] = gamePodUIStrings.getString(key)
                //Log.d("LIVE_STATS_GAMEPODS", "$key: ${gamePodUIStrings.getString(key)}")
            }

            logLIVEStuff("Got game pods")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 50
            }

            val weaponsURL = "https://valorant-api.com/v1/weapons"
            weaponsJSONObject = JSONObject(URL(weaponsURL).readText())

            logLIVEStuff("Got weapons")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 60
            }

            val contenttierURL = "https://valorant-api.com/v1/contenttiers"
            contenttierJSONObject = JSONObject(URL(contenttierURL).readText())

            logLIVEStuff("Got content tiers")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 70
            }

            val weaponSkinsURL = "https://valorant-api.com/v1/weapons/skinlevels/"
            weaponsSkinLevels = JSONObject(URL(weaponSkinsURL).readText())

            logLIVEStuff("Got weapon skins")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 80
            }

            val titlesURL = "https://valorant-api.com/v1/playertitles"
            titlesJSON = JSONObject(URL(titlesURL).readText())

            logLIVEStuff("Got titles")
            withContext(Dispatchers.Main) {
                progressDialog.progress = 90
            }

            withContext(Dispatchers.Main)
            {
                loadUI("LIVE")
            }
        }

        val bg = requireView().findViewById<ImageView>(R.id.new_LiveStatsBackground)
        Picasso.get().load(StaticsMainActivity.playerCardLarge).fit().centerCrop()
            .transform(BlurTransformation(requireContext())).into(bg)

        val materialCardPartyPlayer =
            requireView().findViewById<MaterialCardView>(R.id.materialCardPartyPlayer)
        materialCardPartyPlayer.visibility = View.VISIBLE
        val partyExpandButton = requireView().findViewById<ImageButton>(R.id.partyExpandButton)
        partyExpandButton.setOnClickListener {
            if (partyExpanded) {
                // animate the material card to fade out
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.interpolator = AccelerateInterpolator()
                fadeOut.duration = 300
                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        materialCardPartyPlayer.visibility = View.GONE
                    }

                    override fun onAnimationStart(animation: Animation?) {}
                })
                materialCardPartyPlayer.startAnimation(fadeOut)
                partyExpandButton.animate().rotation(-90f).setDuration(300).setInterpolator {
                    AccelerateInterpolator().getInterpolation(it)
                }.start()
                partyExpanded = false
            } else {
                materialCardPartyPlayer.visibility = View.VISIBLE
                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.interpolator = AccelerateInterpolator()
                fadeIn.duration = 300
                materialCardPartyPlayer.startAnimation(fadeIn)
                partyExpandButton.animate().rotation(0f).setDuration(300).setInterpolator {
                    AccelerateInterpolator().getInterpolation(it)
                }.start()
                partyExpanded = true
            }
        }

        val materialCardPlayerLoadouts =
            requireView().findViewById<MaterialCardView>(R.id.materialCardCurrentLoadout)
        materialCardPlayerLoadouts.visibility = View.VISIBLE
        val loadoutExpandButton =
            requireView().findViewById<ImageButton>(R.id.currentLoadoutExpandButton)
        loadoutExpandButton.setOnClickListener {
            if (loadoutExpanded) {
                // animate the material card to fade out
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.interpolator = AccelerateInterpolator()
                fadeOut.duration = 300
                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        materialCardPlayerLoadouts.visibility = View.GONE
                    }

                    override fun onAnimationStart(animation: Animation?) {}
                })
                materialCardPlayerLoadouts.startAnimation(fadeOut)
                loadoutExpandButton.animate().rotation(-90f).setDuration(300).setInterpolator {
                    AccelerateInterpolator().getInterpolation(it)
                }.start()
                loadoutExpanded = false
            } else {
                materialCardPlayerLoadouts.visibility = View.VISIBLE
                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.interpolator = AccelerateInterpolator()
                fadeIn.duration = 300
                materialCardPlayerLoadouts.startAnimation(fadeIn)
                loadoutExpandButton.animate().rotation(0f).setDuration(300).setInterpolator {
                    AccelerateInterpolator().getInterpolation(it)
                }.start()
                loadoutExpanded = true
            }
        }

        val materialCardPlayerStore =
            requireView().findViewById<MaterialCardView>(R.id.materialCardPlayerStore)
        materialCardPlayerStore.visibility = View.VISIBLE
        val storeExpandButton =
            requireView().findViewById<ImageButton>(R.id.playerStoreExpandButton)
        storeExpandButton.setOnClickListener {
            if (storeExpanded) {
                // animate the material card to fade out
                val fadeOut = AlphaAnimation(1f, 0f)
                fadeOut.interpolator = AccelerateInterpolator()
                fadeOut.duration = 300
                fadeOut.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationRepeat(animation: Animation?) {}
                    override fun onAnimationEnd(animation: Animation?) {
                        materialCardPlayerStore.visibility = View.GONE
                    }

                    override fun onAnimationStart(animation: Animation?) {}
                })
                materialCardPlayerStore.startAnimation(fadeOut)
                storeExpandButton.animate().rotation(-90f).setDuration(300).setInterpolator {
                    AccelerateInterpolator().getInterpolation(it)
                }.start()
                storeExpanded = false
            } else {
                materialCardPlayerStore.visibility = View.VISIBLE
                val fadeIn = AlphaAnimation(0f, 1f)
                fadeIn.interpolator = AccelerateInterpolator()
                fadeIn.duration = 300
                materialCardPlayerStore.startAnimation(fadeIn)
                storeExpandButton.animate().rotation(0f).setDuration(300).setInterpolator {
                    AccelerateInterpolator().getInterpolation(it)
                }.start()
                storeExpanded = true
            }
        }
    }

    private fun LIVE(entitlementToken: String?, accessToken: String?) {
        // Entitlement as X-Riot-Entitlements-JWT
        // Access as Bearer Authorization
        if (entitlementToken == null || accessToken == null) {
            return
        }
        try {
            liveSetup()
            logLIVEStuff("Live setup done")
            timerLiveAPI()
            logLIVEStuff("Timer started")
            getPlayerLoadOuts()
            logLIVEStuff("Loadouts done")
            showStoreFront()
            logLIVEStuff("Storefront done")
            // Throw an exception as a test
//            throw Exception("Hello Discord, this is a test so that next time you get an error\n\nYou do the following:\n\n1. Click on 'Copy & Send'\n2. Send it in channel 'bugs and issues'" +
//                    "\n\nBoom! I'll now be able to help out much more!")

        } catch (e: Exception) {
            // Alert the user and ask to send a bug report
            loadingDialogStatics.dismiss()
            val msg = "${getString(R.string.s47)} ${e.message} ${e.cause} ${e.toString()}"
            val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.s48))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.s50)) { dialog, which ->
                    // copy to clipboard and send to discord
                    val clipboard =
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Valorant Live Stats", msg)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), getString(R.string.s49), Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://discord.gg/jwfJUQMPP7")
                    startActivity(intent)
                }
                .setNegativeButton(getString(R.string.s51), null)
            dialog.show()
        }
    }

    // a kotlin coroutine to get the party status every second
    private fun timerLiveAPI() {
        liveModeScope.launch(Dispatchers.IO) {
            while (true) {
                delay((timerSeconds))
                // context is the main thread
                withContext(Dispatchers.Main)
                {
                    getPartyStatus()
                }
            }
        }
    }

    private suspend fun getContracts() {
        if (PlayerUUID == null) {
            return
        }

        val url = "https://pd.${shard}.a.pvp.net/contracts/v1/contracts/$PlayerUUID"
        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code

        Log.d("LIVE_STATS_CONTRACTS", "Response code: $code Body: $body")


    }

    private fun showStoreFront() {
        val url = "https://pd.${shard}.a.pvp.net/store/v2/storefront/${PlayerUUID}"

        liveModeScope.launch {
            val response = APIRequestValorant(url)
            val body = response.body.string()
            val code = response.code

            if (code != 200) return@launch

            //Log.d("LIVE_STATS_STOREFRONT", "Response code: $code Body: $body")
            withContext(Dispatchers.Main) {
                val json = JSONObject(body)
                val timerSeconds =
                    json.getJSONObject("SkinsPanelLayout")
                        .getInt("SingleItemOffersRemainingDurationInSeconds")
                setStoreTimer(timerSeconds)
                val NightMarket = json.optJSONObject("BonusStore")
                if (NightMarket != null) {
                    //show dialog
                    Snackbar.make(requireView(), getString(R.string.s52), Snackbar.LENGTH_LONG).show()
                    handleNightMarket(NightMarket)
                }
                handleDailyOffers(json.getJSONObject("SkinsPanelLayout"))
                getWallet()
            }
        }
    }

    private fun handleNightMarket(data: JSONObject){

    }

    private fun handleDailyOffers(dailyOffersSkins: JSONObject) {
        val weaponsSkins = ArrayList<WeaponSkinOffer>()

        val offers = dailyOffersSkins.getJSONArray("SingleItemStoreOffers")
        for (i in 0 until offers.length()) {
            val currentOffer = offers.getJSONObject(i)
            val offerID = currentOffer.getString("OfferID")
            val cost =
                currentOffer.getJSONObject("Cost").getInt("85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741")
            val jsonWeapon = getNameAndImageFromOffer(offerID)
            if (jsonWeapon == JSONObject()) {
                return
            }
            val displayName = jsonWeapon.getString("displayName")
            val displayIcon = jsonWeapon.getString("displayIcon")
            val rarity = getRarity(offerID)
            val weaponSkinOffer = WeaponSkinOffer(displayName, displayIcon, cost, rarity)
            weaponsSkins.add(weaponSkinOffer)
        }

        val adapter = WeaponSkinOfferAdapter(requireActivity(), weaponsSkins)
        val listView = requireView().findViewById<ListView>(R.id.playerStoreListView)
        listView.adapter = adapter
    }

    private fun getRarity(offerID: String): JSONObject {
        val data = weaponsJSONObject.getJSONArray("data")
        for (i in 0 until data.length()) {
            val weapon = data.getJSONObject(i)
            val skins = weapon.getJSONArray("skins")
            for (j in 0 until skins.length()) {
                val skin = skins.getJSONObject(j)
                val contentTierUUID = skin.getString("contentTierUuid")
                val levels = skin.getJSONArray("levels")
                for (k in 0 until levels.length()) {
                    val level = levels.getJSONObject(k)
                    val skinID = level.getString("uuid")
                    if (skinID == offerID) {
                        return getContentTier(contentTierUUID)
                    }
                }
            }
        }
        return JSONObject()
    }

    private fun getContentTier(contentID: String): JSONObject {
        val data = contenttierJSONObject.getJSONArray("data")
        for (i in 0 until data.length())
        {
            val contentTierData = data.getJSONObject(i)
            val contentTierID = contentTierData.getString("uuid")
            if (contentTierID == contentID)
            {
                val displayIcon = contentTierData.getString("displayIcon")
                val highlightColour = contentTierData.getString("highlightColor")

                // make a json object with the displayIcon and highlightColour
                val contentTier = JSONObject()
                contentTier.put("displayIcon", displayIcon)
                contentTier.put("highlightColour", highlightColour)
                return contentTier
            }
        }
        return JSONObject()
    }

    data class Weapon(val weaponID: String, val name: String, val imageString: String)

    private fun getNameAndImageFromOffer(offerID: String): JSONObject {
        // run a coroutine to get the name and image from https://valorant-api.com/v1/weapons/skinlevels/${offerID} and return the json
        // then use the json to get the name and image
//        val url = "https://valorant-api.com/v1/weapons/skinlevels/${offerID}"
//        val response = APIRequestValorant(url)
//        val body = response.body.string()
//        val code = response.code
//        if (code != 200) return JSONObject()
//        return JSONObject(body)

        val data = weaponsSkinLevels.getJSONArray("data")
        for (i in 0 until data.length())
        {
            val currentSkin = data.getJSONObject(i)
            if (currentSkin.getString("uuid") == offerID)
            {
                return currentSkin
            }
        }
        return JSONObject()
    }

    private fun setStoreTimer(seconds: Int) {
        val timer = requireView().findViewById<TextView>(R.id.playerStoreTimer)
        if (storeTimer != null) return

        //Toast.makeText(requireContext(), "Store timer set!", Toast.LENGTH_SHORT).show()

        storeTimer = object : CountDownTimer((seconds * 1000).toLong(), 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                val hours = seconds / 3600
                val minutes = (seconds % 3600) / 60
                val secs = seconds % 60
                val time = String.format("%02d:%02d:%02d", hours, minutes, secs)
                timer.text = time
            }

            override fun onFinish() {
                timer.text = "00:00:00"
                storeTimer = null
                showStoreFront()
            }
        }

        storeTimer?.start()
    }

    private fun getPlayerLoadOuts() {
        if (PlayerUUID == null) {
            return
        }
        val url =
            "https://pd.${shard}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
        liveModeScope.launch {
            val response = APIRequestValorant(url)
            val body = response.body.string()
            val code = response.code

            withContext(Dispatchers.Main)
            {
                if (code == 200) {
                    // show dialog
                    val json = JSONObject(body)
                    loadSprays(json.getJSONArray("Sprays"))
                    loadTitle(json.getJSONObject("Identity").getString("PlayerTitleID"))
                    loadPlayerCard(json.getJSONObject("Identity").getString("PlayerCardID"))
                    loadWeapons(json.getJSONArray("Guns"))
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Could not get your load-outs",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getWeaponName(weaponID: String): String {
        val data = weaponsJSONObject.getJSONArray("data")
        for (weapon in data)
        {
            if (weapon.getString("uuid") == weaponID)
            {
                return weapon.getString("displayName")
            }
        }
        return ""
    }

    operator fun JSONArray.iterator(): Iterator<JSONObject>
            = (0 until length()).asSequence().map { get(it) as JSONObject }.iterator()

    private fun loadWeaponsList(weapons: JSONArray): MutableList<Weapon>
    {
        val weaponsList: MutableList<Weapon> = mutableListOf()
        for (weapon in weapons)
        {
            val weaponID = weapon.getString("ID")
            weaponsList += Weapon(weaponID, getWeaponName(weaponID), getWeaponSkinImage(weaponID, weapon.getString("SkinID")))
        }
        return weaponsList
    }

    private fun loadWeapons(weapons: JSONArray)
    {
        //val availableGunSkins = getAvailableGunSkins()
        val weaponsList = loadWeaponsList(weapons)
        val weaponAdapter = CurrentLoadoutWeapon(weaponsList) // Replace with your own weapon data list
        currentLoadoutWeaponsRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        currentLoadoutWeaponsRecyclerView.adapter = weaponAdapter

        weaponAdapter.setOnWeaponClickListener(object : CurrentLoadoutWeapon.OnWeaponClickListener {
            override fun onWeaponClick(weapon: Weapon) {
//                val skins = lookupGunSkins(weapon.weaponID, availableGunSkins)
//                if (skins == null)
//                {
//                    Toast.makeText(requireContext(), "You don't have skins for ${weapon.name} :(", Toast.LENGTH_SHORT).show()
//                    return
//                }
//                else {
//                    copyToClipboard(skins.toString(), "${skins.size} skins for ${weapon.name}")
//                }
                // show an alert dialog saying that you will be able to change the skin soon!
                val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                builder.setTitle("Coming soon!")
                builder.setMessage("You will be able to change your ${weapon.name} skins soon! :)")
                builder.setPositiveButton("Ok") { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()
            }
        })
    }

    private fun lookupGunSkins(weaponID: String, skinsID: List<String>): List<String>?
    {
        val list = mutableListOf<String>()
        val data = weaponsJSONObject.getJSONArray("data")
        for (i in 0 until data.length())
        {
            val weapon = data.getJSONObject(i)
            if (weapon.getString("uuid") == weaponID) {
                val skins = weapon.getJSONArray("skins")
                for (j in 0 until skins.length())
                {
                    var levelsCounter = 0
                    val skin = skins.getJSONObject(j)
                    val levels = skin.getJSONArray("levels")
                    for (k in 0 until levels.length())
                    {
                        val level = levels.getJSONObject(k)
                        if (level.getString("uuid") in skinsID)
                        {
                            list += level.getString("uuid")
                            levelsCounter++
                        }
                    }

                    var chromasCounter = 0
                    val chromas = skin.getJSONArray("chromas")
                    for (k in 0 until chromas.length())
                    {
                        val chroma = chromas.getJSONObject(k)
                        if (chroma.getString("uuid") in skinsID)
                        {
                            list += chroma.getString("uuid")
                            chromasCounter++
                        }
                    }

                    Log.d("LIVE", "Found $levelsCounter levels and $chromasCounter chromas for $weaponID")
                }
                return if (list.isEmpty()) null else list
            }
        }
        return null
    }

    private fun getWeaponSkinImage(weaponID: String, skinID: String): String
    {
        val data = weaponsJSONObject.getJSONArray("data")
        for (i in 0 until data.length())
        {
            val weapon = data.getJSONObject(i)
            val wID = weapon.getString("uuid")
            if (wID == weaponID)
            {
                val skins = weapon.getJSONArray("skins")
                for (j in 0 until skins.length())
                {
                    val skin = skins.getJSONObject(j)
                    val sID = skin.getString("uuid")
                    if (sID == skinID) {
                        return skin.getJSONArray("chromas").getJSONObject(0).getString("fullRender")
                    }
                }
            }
        }
        return ""
    }

    private fun loadPlayerCard(cardID: String) {
        val largeURL = "https://media.valorant-api.com/playercards/${cardID}/largeart.png"
        val smallURL = "https://media.valorant-api.com/playercards/${cardID}/smallart.png"
        val wideURL = "https://media.valorant-api.com/playercards/${cardID}/wideart.png"

        val smolImg = view?.findViewById<ImageView>(R.id.new_playerAvatar)
        val bigImg = view?.findViewById<ImageView>(R.id.new_LiveStatsBackground)
        val currentPlayerCardLoadout = view?.findViewById<ImageView>(R.id.CurrentLoadoutPlayerCard)

        Picasso.get().load(smallURL).fit().centerCrop().into(smolImg)
        Picasso.get().load(largeURL).fit().centerCrop().into(bigImg)
        Picasso.get().load(wideURL).fit().centerCrop().into(currentPlayerCardLoadout)
    }

    private fun updatePlayerCard(selectedPicture: String) {
        val url =
            "https://pd.${shard}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
        liveModeScope.launch {
            val response = APIRequestValorant(url)
            var body = response.body.string()
            val code = response.code

            if (code != 200) return@launch

            val json = JSONObject(body)
            val identity = json.getJSONObject("Identity")
            identity.put("PlayerCardID", selectedPicture)

            // convert to string
            body = json.toString()

            val sprayResponse = APIRequestValorant(url, body, true)
            val sprayBody = sprayResponse.body.string()
            val sprayCode = sprayResponse.code
            withContext(Dispatchers.Main) {
                if (sprayCode == 200) {
                    Snackbar.make(
                        requireView(),
                        getString(R.string.s57),
                        Snackbar.LENGTH_SHORT
                    ).show()
                    val refreshButtonCurrentLoadout =
                        view?.findViewById<ImageView>(R.id.new_refreshButtonCurrentLoadout)
                    refreshButtonCurrentLoadout!!.animate().rotationBy(360f).setDuration(500)
                        .withEndAction {
                            getPlayerLoadOuts()
                        }
                } else {
                    // show error
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Error")
                        .setMessage("Error updating spray: $sprayBody")
                        .setPositiveButton("OK", null)
                    dialog.show()
                }
            }
        }
    }

    private fun loadTitle(TitleID: String) {
        val titleName = getTitleFromJson(TitleID)
        view?.findViewById<TextView>(R.id.CurrentLoadoutTitle)?.text = titleName
        view?.findViewById<ImageView>(R.id.CurrentLoadoutTitleEdit)?.setOnClickListener {
            // get all of the titles from the assets db
            val titles = getAvailableTitles()
            // convert to CharSequence
            val titlesCharSequence = titles.map { it }.toTypedArray()
            // show a dialog with all of the titles
            val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.s58))
                .setItems(titlesCharSequence) { _, which ->
                    // get the title id from the selected item
                    val titleID = assetsDB.retrieveIDTitle(titles[which])
                    // update the title
                    updateTitle(titleID)
                }
                .setNegativeButton(getString(R.string.s51), null)

            // set the items text color to white
            val dialogView = dialog.create()
            dialogView.show()
            dialogView.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
            dialogView.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        }
    }

    private fun getTitleFromJson(titleID: String): String {
        val data = titlesJSON.getJSONArray("data")
        for (i in 0 until data.length())
        {
            val currentTitle = data.getJSONObject(i)
            if (currentTitle.getString("uuid") == titleID)
            {
                return currentTitle.getString("titleText")
            }
        }
        return ""
    }

    private fun updateTitle(titleID: String) {
        val url =
            "https://pd.${shard}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
        liveModeScope.launch {
            val response = APIRequestValorant(url)
            var body = response.body.string()
            val code = response.code

            if (code != 200) return@launch

            val json = JSONObject(body)
            val identity = json.getJSONObject("Identity")
            identity.put("PlayerTitleID", titleID)

            // convert to string
            body = json.toString()

            val titleResponse = APIRequestValorant(url, body, true)
            val titleBody = titleResponse.body.string()
            val titleCode = titleResponse.code

            withContext(Dispatchers.Main) {
            if (titleCode == 200) {
                // update the title
                //loadTitle(titleID)
                Snackbar.make(
                    requireView(),
                    getString(R.string.s57),
                    Snackbar.LENGTH_SHORT
                ).show()
                val refreshButtonCurrentLoadout =
                    view?.findViewById<ImageView>(R.id.new_refreshButtonCurrentLoadout)
                refreshButtonCurrentLoadout!!.animate().rotationBy(360f).setDuration(500)
                    .withEndAction {
                        getPlayerLoadOuts()
                    }
            } else {
                // show error
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("Error updating title: $titleBody")
                    .setPositiveButton("OK", null)
                dialog.show()
            }
        }
    }
    }

    private fun updateSpray(sprayID: String, sprayEquipID: String) {
        val url =
            "https://pd.${shard}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
        liveModeScope.launch {
            val response = APIRequestValorant(url)
            var body = response.body.string()
            val code = response.code

            if (code != 200) return@launch

            val json = JSONObject(body)
            val sprays = json.getJSONArray("Sprays")
            for (i in 0 until sprays.length()) {
                val spray = sprays.getJSONObject(i)
                if (spray.getString("EquipSlotID") == sprayEquipID) {
                    spray.put("SprayID", sprayID)
                }
            }

            // convert to string
            body = json.toString()

            val sprayResponse = APIRequestValorant(url, body, true)
            val sprayBody = sprayResponse.body.string()
            val sprayCode = sprayResponse.code
withContext(Dispatchers.Main) {
            if (sprayCode == 200) {
                Snackbar.make(
                    requireView(),
                    getString(R.string.s57),
                    Snackbar.LENGTH_SHORT
                ).show()
                val refreshButtonCurrentLoadout =
                    view?.findViewById<ImageView>(R.id.new_refreshButtonCurrentLoadout)
                refreshButtonCurrentLoadout!!.animate().rotationBy(360f).setDuration(500)
                    .withEndAction {
                        getPlayerLoadOuts()
                    }
            } else {
                // show error
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage("Error updating spray: $sprayBody")
                    .setPositiveButton("OK", null)
                dialog.show()
            }
        }
    }
    }
    private fun loadSprays(Sprays: JSONArray) {
        val leftSprayImage = view?.findViewById<ImageView>(R.id.imageViewLeftSpray)
        val bottomSprayImage = view?.findViewById<ImageView>(R.id.imageViewBottomSpray)
        val rightSprayImage = view?.findViewById<ImageView>(R.id.imageViewRightSpray)
        val topSprayImage = view?.findViewById<ImageView>(R.id.imageViewTopSpray)

        val LeftSpray = "0814b2fe-4512-60a4-5288-1fbdcec6ca48"
        val BottomSpray = "04af080a-4071-487b-61c0-5b9c0cfaac74"
        val RightSpray = "5863985e-43ac-b05d-cb2d-139e72970014"
        val TopSpray = "7cdc908e-4f69-9140-a604-899bd879eed1"

        for (i in 0 until Sprays.length()) {
            when (Sprays.getJSONObject(i).getString("EquipSlotID")) {
                LeftSpray -> {
                    // Pre Round Spray
                    Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(i).getString("SprayID")])
                        .fit()
                        .centerInside().into(leftSprayImage)
                }

                BottomSpray -> {
                    // Mid Round Spray
                    Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(i).getString("SprayID")])
                        .fit()
                        .centerInside().into(bottomSprayImage)
                }

                RightSpray -> {
                    // Post Round Spray
                    Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(i).getString("SprayID")])
                        .fit()
                        .centerInside().into(rightSprayImage)
                }

                TopSpray -> {
                    // Post Round Spray
                    Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(i).getString("SprayID")])
                        .fit()
                        .centerInside().into(topSprayImage)
                }
            }
        }

        val sprays = getAvailableSprays()
        var alertDialog: AlertDialog = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme).create()
        val dialogView =
            LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_picture_list, null)
        val listViewPictures = dialogView.findViewById<ListView>(R.id.listViewPictures)
        listViewPictures.adapter = PictureListAdapter(
            requireActivity(),
            sprays,
            "Spray"
        ) // Create an adapter for the list of pictures

        alertDialog = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
            .setTitle(getString(R.string.s58))
            .setView(dialogView)
            .setNegativeButton(getString(R.string.s51)) { _, _ ->
                // Handle cancel button click event, if needed
            }
            .create()
        topSprayImage!!.setOnClickListener {
            listViewPictures.setOnItemClickListener { _, _, position, _ ->
                val selectedPicture = sprays[position]
                updateSpray(selectedPicture, TopSpray)
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        leftSprayImage!!.setOnClickListener {
            listViewPictures.setOnItemClickListener { _, _, position, _ ->
                val selectedPicture = sprays[position]
                updateSpray(selectedPicture, LeftSpray)
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        rightSprayImage!!.setOnClickListener {
            listViewPictures.setOnItemClickListener { _, _, position, _ ->
                val selectedPicture = sprays[position]
                updateSpray(selectedPicture, RightSpray)
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        bottomSprayImage!!.setOnClickListener {
            listViewPictures.setOnItemClickListener { _, _, position, _ ->
                val selectedPicture = sprays[position]
                updateSpray(selectedPicture, BottomSpray)
                alertDialog.dismiss()
            }

            alertDialog.show()
        }
    }

    private fun liveSetup() {
        val spinner = view?.findViewById<Spinner>(R.id.new_partyGameModeSelect)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gameModes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        val refreshButtonCurrentLoadout =
            view?.findViewById<ImageButton>(R.id.new_refreshButtonCurrentLoadout)
        refreshButtonCurrentLoadout?.setOnClickListener {
            // animate 360
            refreshButtonCurrentLoadout.animate().rotationBy(360f).setDuration(500).withEndAction {
                getPlayerLoadOuts()
            }
        }

        // spinner listener
        spinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                changeQueue(gameModes[position])
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        val joinMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
        joinMatchButton!!.setOnClickListener {
            if (playerPartyID == null || partyState == null) return@setOnClickListener
            getPartyStatus()
            if (partyState == "MATCHMAKING") {
                // cancel matchmaking
                cancelMatchmaking()
            } else {
                // join matchmaking
                joinMatchmaking()
            }
            getPartyStatus()
        }

        agentPreGameRecyclerView.adapter = ImageAdapter(getAgentImages())

        val logOut = view?.findViewById<ImageButton>(R.id.new_partyPlayerLogOut)
        logOut?.setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle(getString(R.string.s92))
                .setMessage(getString(R.string.s56))
                .setPositiveButton("Yes") { _, _ ->
                    // log out
                    // clear the shared preferences from auth
                    authPreferences.edit().clear().apply()
                    // go to loading screen
                    val intent = Intent(requireActivity(), NewLogInUI::class.java)
                    intent.putExtra("login", "true")
                    startActivity(intent)
                    activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                    activity?.finish()
                }
                .setNegativeButton("No", null)
            dialog.show()
        }

        val avatarPFP = view?.findViewById<ImageView>(R.id.new_playerAvatar)
        val CurrentLoadoutPlayerCard = view?.findViewById<ImageView>(R.id.CurrentLoadoutPlayerCard)
        CurrentLoadoutPlayerCard?.setOnClickListener {
            showPlayerCardsDialog()
        }
        avatarPFP?.setOnClickListener {
            showPlayerCardsDialog()
        }

        val readySwitch = requireView().findViewById<SwitchMaterial>(R.id.new_readySwitch)
        readySwitch?.setOnCheckedChangeListener { _, isChecked -> handleMemberReady(isChecked) }

        // disable the ready switch until the party is ready
        readySwitch.visibility = View.GONE
    }

    private fun showPlayerCardsDialog() {
        val pictures = getAvailablePlayerCards()
        var alertDialog: AlertDialog = AlertDialog.Builder(requireActivity()).create()
        val dialogView =
            LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_picture_list, null)
        val listViewPictures = dialogView.findViewById<ListView>(R.id.listViewPictures)
        listViewPictures.adapter = PictureListAdapter(
            requireActivity(),
            pictures,
            "PlayerCard"
        ) // Create an adapter for the list of pictures

        alertDialog = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
            .setTitle(getString(R.string.s58))
            .setView(dialogView)
            .setNegativeButton(getString(R.string.s51)) { _, _ ->
                // Handle cancel button click event, if needed
            }
            .create()

        listViewPictures.setOnItemClickListener { _, _, position, _ ->
            val selectedPicture = pictures[position]
            updatePlayerCard(selectedPicture)
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    private fun handleMemberReady(ready: Boolean) {
        if (playerPartyID == null) return
        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${playerPartyID}/members/${PlayerUUID}/setReady"
        val body = "{\"ready\": $ready}"
        liveModeScope.launch { APIRequestValorant(url, body) }
    }

    private fun joinMatchmaking() {
        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${playerPartyID}/matchmaking/join"
        liveModeScope.launch {
            val response = APIRequestValorant(url, "")
            val code = response.code
            withContext(Dispatchers.Main) {
                if (code == 200) {
                    val partyStatus = view?.findViewById<TextView>(R.id.new_playerPartyStatus)
                    partyStatus?.text = getString(R.string.s59)
                    val joinMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
                    joinMatchButton?.text = getString(R.string.s60)
                }
            }
        }
    }

    private fun cancelMatchmaking() {
        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${playerPartyID}/matchmaking/leave"
        liveModeScope.launch { APIRequestValorant(url, "") }
    }

    private fun getPartyStatus() {
        try {
            liveModeScope.launch {
                val response =
                    APIRequestValorant("https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/players/${PlayerUUID}")
                val code = response.code
                val body = response.body.string()
                withContext(Dispatchers.Main)
                {
                    if (code == 404 || body.contains("PLAYER_DOES_NOT_EXIST")) {
                        // No party found so player is not on Valorant
                        timerSeconds = 10000
                        notInGame()
                        return@withContext
                    } else if (code == 400 && body.contains("BAD_CLAIMS")) {
                        // auth expired so tell user to restart app
                        timerSeconds = 100000
                        changePartyStatusText(getString(R.string.s61))
                        // restart app
                        val intent = Intent(requireContext(), LoadingActivity::class.java)
                        startActivity(intent)
                        activity?.finish()
                    } else if (code == 200) {
                        timerSeconds = 1000
                        val partyJSON = JSONObject(body)
                        playerPartyID = partyJSON.getString("CurrentPartyID")
                        getPartyDetails(playerPartyID!!)
                    } else {
                        Log.d("LIVE_STATS_PARTY_STATUS", "Error: $code")
                        Log.d("LIVE_STATS_PARTY_STATUS", "Body: $body")
                    }
                }
            }

        } catch (e: Exception) {
            Log.d("LIVE_STATS_PARTY_STATUS", "Error: ${e.message}")
        }
    }

    private fun changePartyStatusText(newText: String) {
        val partyStatus = view?.findViewById<TextView>(R.id.new_playerPartyStatus)
        partyStatus?.text = newText
        fadeRepeat(partyStatus!!)
    }

    private fun sendNotification(title: String, message: String, channel_name: String) {
        // check if the notification has been sent
        if (notificationSent) return

        val notificationManager =
            requireActivity().getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationID = 1
        val channelID = "statics_live"
        val channelName = channel_name
        val importance = NotificationManager.IMPORTANCE_HIGH
        val mChannel = NotificationChannel(channelID, channelName, importance)
        notificationManager.createNotificationChannel(mChannel)
        val mBuilder = NotificationCompat.Builder(requireActivity(), channelID)
            .setSmallIcon(R.drawable.just_statics_alot_smaller)
            .setContentTitle(title)
            .setContentText(message)
        notificationManager.notify(notificationID, mBuilder.build())
        notificationSent = true
    }

    private fun getPings(Pings: JSONArray) {
        val pingListView = view?.findViewById<ListView>(R.id.new_partyPingsListView)
        // get previous pings from ping list view
        val previousPings = pingListView?.adapter as PingListAdapter?
        val previousPingsList = previousPings?.pingList

        val pingList = ArrayList<ValorantPing>()
        for (i in 0 until Pings.length()) {
            val ping = Pings.getJSONObject(i)
            val pingValue = ping.getInt("Ping")
            val gamePodID = ping.getString("GamePodID")
            val translatedGamePodID = GamePodStrings[gamePodID]
            val pingObject = ValorantPing(translatedGamePodID!!, pingValue)
            pingList.add(pingObject)
        }

        // sort the list by ping value
        pingList.sortBy { it.PingValue }

        // if the ping list is the same as the previous ping list then don't update the list view
        if (previousPingsList == pingList) {
            return
        }

        //pingListView?.visibility = View.VISIBLE
        pingListView?.adapter = PingListAdapter(requireActivity(), pingList)

    }

    private fun handlePartyMembers(members: JSONArray) {
        // A list of PartyMember objects
        val partyMembers = ArrayList<PartyMember>()
        val partyMemberListView = view?.findViewById<ListView>(R.id.new_partyMembersListView)

        for (i in 0 until members.length()) {
            val member = members.getJSONObject(i)
            val subject = member.getString("Subject")
            var name = decodeNameFromSubject(subject)
            val playerCardID = member.getJSONObject("PlayerIdentity").getString("PlayerCardID")
            val playerTitleID = member.getJSONObject("PlayerIdentity").getString("PlayerTitleID")
            val playerReady = member.getBoolean("IsReady")
            val isModerator = member.getBoolean("IsModerator")

            if (subject == PlayerUUID) {
                loadPlayerCard(playerCardID)
                getPings(member.getJSONArray("Pings"))
            }

            if (isModerator) name = "(Leader) $name"

            partyMembers.add(
                PartyMember(
                    name.split("#")[0],
                    name.split("#")[1],
                    playerTitleID,
                    playerCardID,
                    playerReady,
                    region
                )
            )
        }

        if (members.length() == 1) {
            // only one member so hide the listview
            view?.findViewById<TextView>(R.id.new_partyMembersText)?.text = getString(R.string.s62)
            partyMemberListView?.visibility = View.INVISIBLE
            // clear the listview
            partyMemberListView?.adapter = null
            return
        } else {
            partyMemberListView?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.new_partyMembersText)?.text =
                "${members.length()} ${getString(R.string.s63)}"
            partyMemberListView?.adapter = PartyMemberAdapter(requireActivity(), partyMembers)
        }
    }

    private fun decodeNameFromSubject(subject: String): String {
        // check if auth preferences contains the subject
        if (authPreferences.contains(subject)) {
            // return the name
            return authPreferences.getString(subject, "")!!
        } else {
            // get the name from the subject
            val url = "https://pd.${shard}.a.pvp.net/name-service/v2/players"
            // body is an array of subjects
            val body = "[\"$subject\"]"
            return runBlocking(Dispatchers.IO)  {
                val response = APIRequestValorant(url, body, true)
                val code = response.code
                val subjectBody = response.body.string()

                if (code != 200) return@runBlocking ""
                val playerName = JSONArray(subjectBody).getJSONObject(0).getString("GameName")
                val playerTag = JSONArray(subjectBody).getJSONObject(0).getString("TagLine")

                val displayName = "$playerName#$playerTag"

                // save the name to the shared preferences
                authPreferences.edit().putString(subject, displayName).apply()
                return@runBlocking displayName
            }
        }
    }

    private fun getPartyDetails(partyID: String) {
        val url = "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${partyID}"
        liveModeScope.launch {
            val response = APIRequestValorant(url)
            val body = response.body.string()
            val code = response.code
            if (code != 200) return@launch
            partyState = JSONObject(body).getString("State")
            val previousState = JSONObject(body).getString("PreviousState")
            val currentModeSelected =
                JSONObject(body).getJSONObject("MatchmakingData").getString("QueueID")

            var isReady = true
            withContext(Dispatchers.Main)
            {
                val members = JSONObject(body).getJSONArray("Members")
                handlePartyMembers(members)
                for (i in 0 until members.length()) {
                    if (!members.getJSONObject(i).getBoolean("IsReady")) isReady = false
                    break
                }

                val spinner = view?.findViewById<Spinner>(R.id.new_partyGameModeSelect)
                val currentModeSelectedIndex = gameModes.indexOf(capitaliseGameMode(currentModeSelected))
                spinner?.setSelection(currentModeSelectedIndex)
                handlePartyState(partyState!!, previousState, isReady)
                //Log.d("LIVE_STATS_PARTY_STATUS", "Party state: $body")
            }
        }
    }

    private fun capitaliseGameMode(mode:String): String
    {
        var currentModeSelectedCapital =
            mode[0].uppercaseChar() + mode.substring(1)
        if (currentModeSelectedCapital == "Spikerush") currentModeSelectedCapital =
            getString(R.string.s147)
        if (currentModeSelectedCapital == "Swiftplay") currentModeSelectedCapital =
            getString(R.string.s152)
        if (currentModeSelectedCapital == "Ggteam") currentModeSelectedCapital =
            getString(R.string.s151)
        if (currentModeSelectedCapital == "Onefa") currentModeSelectedCapital =
            getString(R.string.s150)
        return currentModeSelectedCapital
    }

    private fun handlePartyState(
        state: String,
        previousState: String? = null,
        isReady: Boolean? = null
    ) {
        val joinMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
        joinMatchButton!!.alpha = 1.0f
        joinMatchButton.isEnabled = true
        if (state == "MATCHMAKING") {
            joinMatchButton.text = getString(R.string.s60)
            changePartyStatusText(getString(R.string.s59))
            hideLayoutsMatch()
            val readySwitch = requireView().findViewById<SwitchMaterial>(R.id.new_readySwitch)
            readySwitch.visibility = View.VISIBLE
        } else if (state == "DEFAULT" && previousState == "LEAVING_MATCHMAKING" || previousState == "DEFAULT") {
            joinMatchButton.text = getString(R.string.s64)
            hideLayoutsMatch()
            changePartyStatusText(getString(R.string.s65))
            val readySwitch = requireView().findViewById<SwitchMaterial>(R.id.new_readySwitch)
            readySwitch.visibility = View.VISIBLE
        } else if (state == "DEFAULT" && previousState == "MATCHMADE_GAME_STARTING" && !isReady!!) {
            changePartyStatusText(getString(R.string.s66))
            joinMatchButton.text = getString(R.string.s67)
            joinMatchButton.alpha = 0.5f
            joinMatchButton.isEnabled = false
            getGameInfoPlayer()
        } else if (state == "DEFAULT" && previousState == "MATCHMADE_GAME_STARTING" && isReady!!) {
            joinMatchButton.text = getString(R.string.s64)
            hideLayoutsMatch()
            changePartyStatusText(getString(R.string.s65))
            val readySwitch = requireView().findViewById<SwitchMaterial>(R.id.new_readySwitch)
            readySwitch.visibility = View.VISIBLE
        }
    }

    private fun notInGame() {
        // check if context is null
        if (context == null) return
        changePartyStatusText(getString(R.string.s53))
        val findMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
        findMatchButton!!.alpha = 0.5f
        findMatchButton.isEnabled = false

        val PingsList = view?.findViewById<ListView>(R.id.new_partyPingsListView)
        PingsList?.visibility = View.GONE

        val new_partyMembersText = view?.findViewById<TextView>(R.id.new_partyMembersText)
        new_partyMembersText?.text = getString(R.string.s54)

        val readySwitch = requireView().findViewById<SwitchMaterial>(R.id.new_readySwitch)
        readySwitch.visibility = View.GONE

        playerPartyID = null
        partyState = null
    }

    private fun getGameInfoPlayer() {
        val PreGameLayout = view?.findViewById<RelativeLayout>(R.id.new_LayoutPartyPreGame)

        if (PlayerUUID == null) return
        val url = "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/players/${PlayerUUID}"

        liveModeScope.launch {
            val response = APIRequestValorant(url)
            val code = response.code
            val body = response.body.string()
            withContext(Dispatchers.Main){
            val readySwitch = requireView().findViewById<SwitchMaterial>(R.id.new_readySwitch)
            readySwitch.visibility = View.GONE

            if (code == 200) {
                val preGameJSON = JSONObject(body)
                val matchID = preGameJSON.getString("MatchID")
                PreGameLayout?.visibility = View.VISIBLE
                playerPreGame(matchID)
            } else if (code == 404) {
                notificationSent = false
                PreGameLayout?.visibility = View.GONE
                val coreGameURL =
                    "https://glz-${region}-1.${shard}.a.pvp.net/core-game/v1/players/${PlayerUUID}"
                val coreGameResponse = APIRequestValorant(coreGameURL)
                val coreGameCode = coreGameResponse.code
                val coreGameBody = coreGameResponse.body.string()

                if (coreGameCode == 200) {
                    val coreGameJSON = JSONObject(coreGameBody)
                    val matchID = coreGameJSON.getString("MatchID")
                    playerCoreGame(matchID)
                }
            }
        }
        }
    }

    private fun hideLayoutsMatch() {
        val pregameLayout = view?.findViewById<RelativeLayout>(R.id.new_LayoutPartyPreGame)
        pregameLayout?.visibility = View.GONE
        val lockInButton = view?.findViewById<Button>(R.id.new_lockInButton)
        lockInButton!!.alpha = 1.0f
        lockInButton.isEnabled = true
        lockInButton.text = getString(R.string.s68)
    }

    private fun getWallet() {
        val url = "https://pd.${shard}.a.pvp.net/store/v1/wallet/${PlayerUUID}"
        liveModeScope.launch {
            val response = APIRequestValorant(url)
            val code = response.code
            val body = response.body.string()

            if (code != 200) return@launch
            withContext(Dispatchers.Main)
            {
                val walletJSON = JSONObject(body).getJSONObject("Balances")
                val VP = walletJSON.getInt("85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741")
                val RadiantePoints = walletJSON.getInt("e59aa87c-4cbf-517a-5983-6e81511be9b7")

                val VPText = view?.findViewById<TextView>(R.id.VPTextBalance)
                val RadiantePointsText = view?.findViewById<TextView>(R.id.RPTextBalance)

                VPText?.text = VP.toString()
                RadiantePointsText?.text = RadiantePoints.toString()

                Picasso.get()
                    .load("https://media.valorant-api.com/currencies/85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741/displayicon.png")
                    .into(view?.findViewById<ImageView>(R.id.VPImage))
                Picasso.get()
                    .load("https://media.valorant-api.com/currencies/e59aa87c-4cbf-517a-5983-6e81511be9b7/displayicon.png")
                    .into(view?.findViewById<ImageView>(R.id.RPImage))
            }
        }

        //Log.d("LIVE_STATS_WALLET", "Wallet: $body")
    }

    private fun playerPreGame(matchID: String) {
        val url = "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/matches/${matchID}"
        liveModeScope.launch {
            val response = APIRequestValorant(url)

            val code = response.code
            val body = response.body.string()
            if (code != 200) return@launch
            withContext(Dispatchers.Main){
                val lockInButton = view?.findViewById<Button>(R.id.new_lockInButton)
                lockInButton!!.setOnClickListener {
                    // get the text from the lock in button
                    val lockInButtonText = lockInButton.text.toString()
                    // get the last word of the text
                    val lockInButtonTextLastWord = lockInButtonText.split(" ").last()
                    // check if the last word is in the agents ID hashmap
                    if (AgentNamesID.containsValue(lockInButtonTextLastWord)) {
                        // get the key of the last word
                        val agentID =
                            AgentNamesID.filterValues { it == lockInButtonTextLastWord }.keys.first()
                        // select the character
                        lockInCharacter(agentID)
                    }
                }

                val quitButton = view?.findViewById<Button>(R.id.new_quitButton)
                quitButton!!.setOnClickListener {
                    quitMatch(matchID)
                }

                val preGameJSON = JSONObject(body)
    //        val allyTeamJSON = preGameJSON.getJSONArray("Players")

                val currentModeSelected = capitaliseGameMode(preGameJSON.getString("QueueID"))
                val textViewMode = view?.findViewById<TextView>(R.id.new_partyPreGameTitle)
                textViewMode?.text = "Playing ${currentModeSelected}"

                sendNotification(
                    "${getString(R.string.s69)} $currentModeSelected",
                    getString(R.string.s70),
                    "match_found"
                )

                val mapName = preGameJSON.getString("MapID")
                val pregameBackground = view?.findViewById<ImageView>(R.id.new_partyPreGameMapImage)
                Picasso.get().load(MapsImagesID[mapName]).fit().centerCrop().into(pregameBackground)
        }
    }

    }

    private fun quitMatch(matchID: String) {
        // Show a dialog to confirm the user wants to quit the match
        val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        builder.setTitle(getString(R.string.s72))
        builder.setMessage(getString(R.string.s71))
        builder.setPositiveButton("Yes") { dialog, which ->
            // get the match ID
            val url =
                "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/matches/${matchID}/quit"
            liveModeScope.launch { APIRequestValorant(url, "") }
        }
        builder.setNegativeButton("No") { dialog, which ->
            // do nothing
        }
        val dialog: AlertDialog = builder.create()
        dialog.show()
    }

    private fun selectCharacter(agentID: String) {
        // check if the pregame layout is visible
        val preGameLayout = view?.findViewById<RelativeLayout>(R.id.new_LayoutPartyPreGame)
        if (preGameLayout?.visibility == View.GONE) return

        if (PlayerUUID == null) return
        val preGameUrl =
            "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/players/${PlayerUUID}"

        liveModeScope.launch {
            val preGameresponse = APIRequestValorant(preGameUrl)
            val preGamecode = preGameresponse.code
            val preGamebody = preGameresponse.body.string()

            if (preGamecode != 200) return@launch

            val preGameJSON = JSONObject(preGamebody)
            val matchID = preGameJSON.getString("MatchID")

            val url =
                "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/matches/${matchID}/select/${agentID}"
            val response = APIRequestValorant(url, "")

            val code = response.code

            withContext(Dispatchers.Main) {
                if (code == 200) {
                    val agentName = AgentNamesID[agentID]
                    changePartyStatusText("${getString(R.string.s73)} $agentName")
                    val agentButton = view?.findViewById<Button>(R.id.new_lockInButton)
                    agentButton?.text = "${getString(R.string.s74)} $agentName"
                }
            }
        }
    }

    private fun lockInCharacter(agentID: String) {
        // check if the pregame layout is visible
        val preGameLayout = view?.findViewById<RelativeLayout>(R.id.new_LayoutPartyPreGame)
        if (preGameLayout?.visibility == View.GONE) return

        if (PlayerUUID == null) return
        val preGameUrl =
            "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/players/${PlayerUUID}"

        liveModeScope.launch {
            val preGameresponse = APIRequestValorant(preGameUrl)
            val preGamecode = preGameresponse.code
            val preGamebody = preGameresponse.body.string()

            if (preGamecode != 200) return@launch

            val preGameJSON = JSONObject(preGamebody)
            val matchID = preGameJSON.getString("MatchID")

            val url =
                "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/matches/${matchID}/lock/${agentID}"
            val response = APIRequestValorant(url, "")

            val code = response.code
            withContext(Dispatchers.Main)
            {
                if (code == 200) {
                    val agentName = AgentNamesID[agentID]
                    changePartyStatusText("${getString(R.string.s73)} $agentName")
                    val lockInButton = view?.findViewById<Button>(R.id.new_lockInButton)
                    lockInButton?.isEnabled = false
                    lockInButton?.alpha = 0.5f
                    lockInButton?.text = "${getString(R.string.s75)} $agentName"
                }
            }
        }
    }

    private fun playerCoreGame(matchID: String) {

    }

    private fun sortHashMapByValues(hashMap: HashMap<String, String>): HashMap<String, String> {
        val sortedList = hashMap.toList().sortedBy { it.second }
        val sortedHashMap = LinkedHashMap<String, String>()
        for ((key, value) in sortedList) {
            sortedHashMap[key] = value
        }
        return sortedHashMap
    }


    private fun getAgentImages(): ArrayList<String> {
        val agentImages = ArrayList<String>()
        // go thru hashmap
        // sort hashmap by Agent names
        val sortedAgents = sortHashMapByValues(AgentNamesID)
//        for (agentName in sortedAgents.values)
//        {
//            Log.d("LIVE_AGENT_HASHMAP", agentName)
//        }

        for (agent in sortedAgents.keys) {
            agentImages.add(agent)
        }
        return agentImages
    }

    private fun changeQueue(mode: String) {
        if (playerPartyID == null) return
        var queueID = mode.lowercase().replace(" ", "")
        if (queueID == "escalation") queueID = "ggteam"
        if (queueID == "replication") queueID = "onefa"

        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${playerPartyID}/queue"
        val body = "{\"queueId\":\"${queueID}\"}"

        liveModeScope.launch {
            val response = APIRequestValorant(url, body)
            val code = response.code
            if (code == 200) {
                withContext(Dispatchers.Main)
                {
                    changePartyStatusText("${getString(R.string.s76)} $mode")
                }
            }
        }

    }
    private fun loadUI(mode: String) {
        CurrentScreen = mode
        when (CurrentScreen) {
            "INIT" -> {
                INITVIEW.visibility = View.VISIBLE
                LIVEVIEW.visibility = View.GONE
                //INITMode()
            }
            "LIVE" -> {
                INITVIEW.visibility = View.GONE
                LIVEVIEW.visibility = View.VISIBLE
                LIVE(entitlementToken, accessToken)
            }
            "ERROR" -> {
                INITVIEW.visibility = View.GONE
                LIVEVIEW.visibility = View.GONE
            }
        }
    }

    private fun fadeRepeat(view: View) {
        // repeat forever fade in and out
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.interpolator = AccelerateInterpolator()
        fadeIn.duration = 500
        fadeIn.repeatCount = 2
        fadeIn.repeatMode = Animation.REVERSE
        view.startAnimation(fadeIn)
    }

    private fun getAvailableSprays(): ArrayList<String> {
        val availableSprays = arrayListOf<String>()
        val sprayID = "d5f120f8-ff8c-4aac-92ea-f2b5acbe9475"
        val url = "https://pd.${shard}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$sprayID"

        return runBlocking(Dispatchers.IO){
            val response = APIRequestValorant(url)
            val body = response.body.string()
            val code = response.code
            if (code != 200) return@runBlocking arrayListOf()
            val sprays = JSONObject(body).getJSONArray("Entitlements")
            for (i in 0 until sprays.length()) {
                val sprayObject = sprays.getJSONObject(i)
                availableSprays.add(sprayObject.getString("ItemID"))
            }
            return@runBlocking availableSprays
        }

    }

    private fun getAvailableAgents(): ArrayList<String>
    {
        val availableAgents = arrayListOf<String>()
        val agentID = "01bb38e1-da47-4e6a-9b3d-945fe4655707"
        val url = "https://pd.${shard}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$agentID"

        return runBlocking(Dispatchers.IO) {
            val response = APIRequestValorant(url)
            val body = response.body.string()
            val code = response.code

            if (code != 200) return@runBlocking arrayListOf()

            Log.d("LIVE_STATS_AVAILABLE_AGENTS", body)
            val agents = JSONObject(body).getJSONArray("Entitlements")
            for (i in 0 until agents.length()) {
                val agentObject = agents.getJSONObject(i)
                val agentID = agentObject.getString("ItemID")
                availableAgents.add(agentID)
            }
            return@runBlocking availableAgents
        }
    }

    private fun getAvailableGunSkins(): ArrayList<String>
    {
        val availableGunSkins = arrayListOf<String>()

        val gunSkinID = "e7c63390-eda7-46e0-bb7a-a6abdacd2433"
        val url = "https://pd.${shard}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$gunSkinID"

        return runBlocking(Dispatchers.IO) {
            val response = APIRequestValorant(url)
            val body = response.body.string()
            val code = response.code

            val skinVarientsID = "3ad1b2b2-acdb-4524-852f-954a76ddae0a"
            val GunSkinVarientsurl = "https://pd.${shard}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$skinVarientsID"

            val GunSkinVarientsresponse = APIRequestValorant(GunSkinVarientsurl)
            val GunSkinVarientsbody = GunSkinVarientsresponse.body.string()
            val GunSkinVarientscode = GunSkinVarientsresponse.code

            if (code != 200 || GunSkinVarientscode != 200) return@runBlocking arrayListOf()
            //copyToClipboard(body + "\n\n" + GunSkinVarientsbody, "Gun Skins")
            val gunSkins = JSONObject(body).getJSONArray("Entitlements")
            for (i in 0 until gunSkins.length()) {
                val gunSkinObject = gunSkins.getJSONObject(i)
                val gunSkinID = gunSkinObject.getString("ItemID")
                availableGunSkins.add(gunSkinID)
            }

            //Toast.makeText(requireContext(), "Got ${gunSkins.length()} gun skins", Toast.LENGTH_SHORT).show()

            val gunVarientSkins = JSONObject(GunSkinVarientsbody).getJSONArray("Entitlements")
            for (i in 0 until gunVarientSkins.length()) {
                val gunVarientSkinObject = gunVarientSkins.getJSONObject(i)
                val gunVarientSkinID = gunVarientSkinObject.getString("ItemID")
                availableGunSkins.add(gunVarientSkinID)
            }

//        Toast.makeText(requireContext(), "Got ${gunVarientSkins
//            .length()} gun variant skins", Toast.LENGTH_SHORT).show()

            //copyToClipboard(availableGunSkins, "Available gun skins")
            return@runBlocking availableGunSkins
        }
    }

    private fun copyToClipboard(content: Any, desc: String = "")
    {
//        val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//        val clip = ClipData.newPlainText("Copied Text", content.toString())
//        clipboard.setPrimaryClip(clip)
//        Toast.makeText(requireContext(), "Copied $desc", Toast.LENGTH_SHORT).show()
    }

    private fun getAvailablePlayerCards(): ArrayList<String> {
        val availablePlayerCards = arrayListOf<String>()
        val playerCardID = "3f296c07-64c3-494c-923b-fe692a4fa1bd\t"
        val url = "https://pd.${shard}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$playerCardID"

        return runBlocking(Dispatchers.IO) {
            val response = APIRequestValorant(url)
            val body = response.body.string()
            val code = response.code

            if (code != 200) return@runBlocking arrayListOf()

            Log.d("LIVE_STATS_AVAILABLE_PLAYERCARDS", body)
            val playerCards = JSONObject(body).getJSONArray("Entitlements")
            for (i in 0 until playerCards.length()) {
                val playerCardObject = playerCards.getJSONObject(i)
                val playerCardID = playerCardObject.getString("ItemID")
                availablePlayerCards.add(playerCardID)
            }
            return@runBlocking availablePlayerCards
        }
    }

    private fun getAvailableTitles(): ArrayList<String> {
        val availableTitles = arrayListOf<String>()
        val titleID = "de7caa6b-adf7-4588-bbd1-143831e786c6\t"
        val url = "https://pd.${shard}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$titleID"

        return runBlocking(Dispatchers.IO) {

            val response = APIRequestValorant(url)
            val body = response.body.string()
            val code = response.code

            if (code != 200) return@runBlocking arrayListOf()

            Log.d("LIVE_STATS_AVAILABLE_TITLES", body)

            val titles = JSONObject(body).getJSONArray("Entitlements")
            for (i in 0 until titles.length()) {
                val titleObject = titles.getJSONObject(i)
                val titleID = titleObject.getString("ItemID")

                // convert titleID to title name
                val converted = getTitleFromJson(titleID)
                availableTitles.add(converted)
            }
            // sort titles alphabetically
            availableTitles.sort()
            return@runBlocking availableTitles
        }

    }


    private inner class ImageAdapter(private val images: List<String>) :
        RecyclerView.Adapter<ImageViewHolder>() {
        private var selectedItem = RecyclerView.NO_POSITION // Store the selected item position

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.agent_pregame_grid_item, parent, false)
            return ImageViewHolder(view)
        }

        override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
            val image = images[position]

            // Bind the image data and isSelected value to ViewHolder
            holder.bind(image, position == selectedItem)

            // Set click listener for the ViewHolder
            holder.setOnItemClickListener(object : ImageViewHolder.OnItemClickListener {
                override fun onItemClick(position: Int) {
                    // Update the selected item position and notify data changes
                    selectedItem = position
                    notifyDataSetChanged()

                    val images = getAgentImages()
                    selectCharacter(images[position])
                }
            })
        }

        override fun getItemCount(): Int {
            return images.size
        }
    }

    private fun APIRequestValorant2(
        url: String,
        body: String? = null,
        put: Boolean? = false
    ): Response {

        // get date in format of YYYY-MM-DD HH:MM:SS
        val date = Calendar.getInstance().time
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)

        if (body == null) {
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Riot-Entitlements-JWT", entitlementToken!!)
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                .addHeader("X-Riot-ClientVersion", ClientVersion)
                .addHeader("Authorization", "Bearer $accessToken")
                .get()
                .build()

            // return main thread blocking
            return runBlocking(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                val responseCode = response.code
                val responseBody = response.body.string()
                RequestLogsDatabase.addLog(url, "GET", dateTime, responseCode, responseBody)
                return@runBlocking client.newCall(request).execute()
            }
        } else if (put == false) {
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Riot-Entitlements-JWT", entitlementToken!!)
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                .addHeader("X-Riot-ClientVersion", ClientVersion)
                .addHeader("Authorization", "Bearer $accessToken")
                .post(body.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            // return main thread blocking
            return runBlocking(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                val responseCode = response.code
                val responseBody = response.body.string()
                RequestLogsDatabase.addLog(url, "POST", dateTime, responseCode, responseBody)
                return@runBlocking client.newCall(request).execute()
            }
        } else {
            val request = Request.Builder()
                .url(url)
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Riot-Entitlements-JWT", entitlementToken!!)
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                .addHeader("X-Riot-ClientVersion", ClientVersion)
                .addHeader("Authorization", "Bearer $accessToken")
                .put(body.toRequestBody("application/json".toMediaTypeOrNull()))
                .build()

            // return main thread blocking
            return runBlocking(Dispatchers.IO) {
                val response = client.newCall(request).execute()
                val responseCode = response.code
                val responseBody = response.body.string()
                RequestLogsDatabase.addLog(url, "PUT", dateTime, responseCode, responseBody)
                return@runBlocking client.newCall(request).execute()
            }
        }
    }

    private suspend fun APIRequestValorant(
        url: String,
        body: String? = null,
        put: Boolean? = false
    ): Response {
        // get date in format of YYYY-MM-DD HH:MM:SS
        val date = Calendar.getInstance().time
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Riot-Entitlements-JWT", entitlementToken!!)
            .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
            .addHeader("X-Riot-ClientVersion", ClientVersion)
            .addHeader("Authorization", "Bearer $accessToken")

        when {
            body == null -> request.get()
            put == false -> request.post(body.toRequestBody("application/json".toMediaTypeOrNull()))
            else -> request.put(body.toRequestBody("application/json".toMediaTypeOrNull()))
        }

        return withContext(Dispatchers.IO) {
            val response = client.newCall(request.build()).execute()
//            val responseCode = response.code
//            val responseBody = response.body.string()
//            RequestLogsDatabase.addLog(url, response.protocol.toString(), dateTime, responseCode, responseBody)
            response
        }
    }

    private fun APIRequestValorant3(
        url: String,
        body: String? = null,
        put: Boolean? = false,
        callback: (Response) -> Unit
    ) {
        // get date in format of YYYY-MM-DD HH:MM:SS
        val date = Calendar.getInstance().time
        val dateTime = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date)

        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Riot-Entitlements-JWT", entitlementToken!!)
            .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
            .addHeader("X-Riot-ClientVersion", ClientVersion)
            .addHeader("Authorization", "Bearer $accessToken")

        when {
            body == null -> request.get()
            put == false -> request.post(body.toRequestBody("application/json".toMediaTypeOrNull()))
            else -> request.put(body.toRequestBody("application/json".toMediaTypeOrNull()))
        }

        val call = client.newCall(request.build())
        call.enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseCode = response.code
                val responseBody = response.body.string()
                RequestLogsDatabase.addLog(url, "", dateTime, responseCode, responseBody)
                callback(response)
            }

            override fun onFailure(call: Call, e: IOException) {
                // Handle network failure or API call errors
                // You can pass an error response or throw an exception if needed
                e.message?.let { Response.Builder().code(0).message(it).build() }
                    ?.let { callback(it) }
            }
        })
    }


    // override when application is not in focus
    override fun onPause() {
        super.onPause()
        timerSeconds = 1000
    }

    override fun onResume() {
        super.onResume()
        timerSeconds = 1000
    }
}

class PingListAdapter(private val context: Context, pingList: ArrayList<ValorantPing>) :
    BaseAdapter() {
    val pingList: ArrayList<ValorantPing> = pingList
    override fun getCount(): Int {
        return pingList.size
    }

    override fun getItem(position: Int): Any {
        return pingList[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =
            LayoutInflater.from(context).inflate(R.layout.ping_list_item, parent, false)
        val pingName = view.findViewById<TextView>(R.id.ServerName)
        val pingImage = view.findViewById<ImageView>(R.id.SignalStrength)
        val pingValue = view.findViewById<TextView>(R.id.ServerPing)

        val currentPing = pingList[position]
        pingName.text = currentPing.ServerName
        pingValue.text = currentPing.PingValue.toString()

        if (currentPing.PingValue <= 45) {
            pingImage.setImageResource(R.drawable.signalstengthfull)
        } else if (currentPing.PingValue <= 90) {
            pingImage.setImageResource(R.drawable.signalstengthmedium)
        } else {
            pingImage.setImageResource(R.drawable.signalstengthlow)
        }

        return view
    }

}

class PictureListAdapter(
    private val context: Context,
    private val pictures: List<String>,
    private val type: String
) : BaseAdapter() {

    override fun getCount(): Int {
        return pictures.size
    }

    override fun getItem(position: Int): Any {
        return pictures[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view =
            LayoutInflater.from(context).inflate(R.layout.image_holder_for_list_view, parent, false)
        val imageView = view.findViewById<ImageView>(R.id.image_view)
        val currentImage = pictures[position]
        var fullURL = ""
        if (type == "Spray") {
            fullURL = "https://media.valorant-api.com/sprays/$currentImage/fulltransparenticon.png"
        } else if (type == "PlayerCard") {
            fullURL = "https://media.valorant-api.com/playercards/$currentImage/smallart.png"
        }

        Picasso.get().load(fullURL).fit().into(imageView)
        return view
    }
}


class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val imageView: ImageView = itemView.findViewById(R.id.image_view)

    fun bind(image: String, isSelected: Boolean) {
        // Set the image resource and alpha value
        val fullURL = "https://media.valorant-api.com/agents/${image}/displayicon.png"
        Picasso.get().load(fullURL).fit().into(imageView)
        imageView.alpha = if (isSelected) 1.0f else 0.6f

        // Add or remove a border based on isSelected value
        if (isSelected) {
            // do an animation of the background resource making it zoom in
            imageView.setBackgroundResource(R.drawable.border_selected)
            imageView.animate().scaleX(1.1f).scaleY(1.1f).setDuration(200).start()
        } else {
            imageView.setBackgroundResource(0) // Set 0 for no border
            imageView.animate().scaleX(1.0f).scaleY(1.0f).setDuration(200).start()
        }

        // Set click listener to handle image click event
        imageView.setOnClickListener {
            // Notify the adapter of the click event
            adapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { position ->
                onItemClickListener?.onItemClick(position)
            }
        }
    }

    // Interface for click listener
    interface OnItemClickListener {
        fun onItemClick(position: Int)
    }

    private var onItemClickListener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        onItemClickListener = listener
    }
}
