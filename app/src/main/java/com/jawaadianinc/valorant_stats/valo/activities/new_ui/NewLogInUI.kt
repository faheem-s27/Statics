package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import ValorantMatch
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.BuildConfig
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityNewLogInUiBinding
import com.jawaadianinc.valorant_stats.main.ValorantApiService
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.Database.ContentLocalisationDatabase
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.ValorantLocalisation.Contents
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URL

class NewLogInUI : AppCompatActivity() {
    lateinit var binding: ActivityNewLogInUiBinding
    lateinit var webView: WebView
    lateinit var updateText: TextView
    private val clientPlatformToken =
        "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9"
    var RiotURL =
        "https://auth.riotgames.com/authorize?redirect_uri=https%3A%2F%2Fplayvalorant.com%2Fopt_in&client_id=play-valorant-web-prod&response_type=token%20id_token&nonce=1&scope=account%20openid"

    var region = ""
    lateinit var ValorantApiService: ValorantApiService

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewLogInUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.riotSignInWebVIEW
        updateText = binding.statusText

        webView.settings.javaScriptEnabled = true
        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                if (request != null) {
                    Log.d("RiotSignIn", request.url.toString())
                }

                val url = request!!.url.toString()

                // check if the url contains the code
                if (url.contains("access_token=")) {
                    val cookies = CookieManager.getInstance().getCookie(RiotURL)
                    val accessToken = url.split("access_token=")[1].split("&")[0]
                    val idToken = url.split("id_token=")[1].split("&")[0]
                    authoriseUser(accessToken, cookies, idToken)
                }
                // else if the url doesn't contain the code, load the url
                else {
                    view!!.loadUrl(url)
                }
                return false
            }
        }
        //val url = "https://auth.riotgames.com/authorize?client_id=statics&redirect_uri=https://statics-fd699.web.app/authorize.html&response_type=code&scope=openid+offline_access&prompt=login"

        // get the intent string and see if it says "login"
        val intentString = intent.getStringExtra("login")
        if (intentString == "true") {
            RiotURL += "&prompt=login"
        }

        // check if its the first time the user has opened the new UI
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val firstTime = prefs.getBoolean("firstTimeRSO", true)
        if (firstTime)
        {
            RiotURL = "https://auth.riotgames.com/authorize?redirect_uri=https%3A%2F%2Fplayvalorant.com%2Fopt_in&client_id=play-valorant-web-prod&response_type=token%20id_token&nonce=1&scope=account%20openid&prompt=login"
            // Show a dialog to the user that says "Make sure to click on 'Stay signed in' when logging in"
            val dialog = MaterialAlertDialogBuilder(this)
            dialog.setTitle(getString(R.string.s236))
            dialog.setMessage(getString(R.string.s237))
            dialog.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }
            dialog.show()
            // set firstTime to false
            prefs.edit().putBoolean("firstTimeRSO", false).apply()
        }

        // track cookies
        CookieManager.getInstance().setAcceptCookie(true)
        webView.loadUrl(RiotURL)

//        val key = BuildConfig.RIOT_API_KEY
//
//        val interceptor = Interceptor { chain ->
//            val newRequest = chain.request().newBuilder()
//                .addHeader("X-Riot-Token", key)
//                .build()
//            chain.proceed(newRequest)
//        }
//
//        val okHttpClient = OkHttpClient.Builder()
//            .addInterceptor(interceptor)
//            .build()
//
//        val retrofit = Retrofit.Builder()
//            .baseUrl("https://eu.api.riotgames.com/")
//            .client(okHttpClient)
//            .addConverterFactory(GsonConverterFactory.create())
//            .build()
//
//        ValorantApiService = retrofit.create(ValorantApiService::class.java)

    }

    private fun addStringToTextView(text: String)
    {
        updateText.text = updateText.text.toString() + "\n" + text
    }

    fun authoriseUser(accessToken: String, cookies: String, idToken: String)
    {
        val DURATION = 10L

        // hide the webview
        webView.alpha = 0f
        // make a loading progress dialog in the middle of the screen
        GlobalScope.launch {
            withContext(Dispatchers.Main)
            {
                addStringToTextView(getString(R.string.got_cookies))
            }
            delay(DURATION)
            withContext(Dispatchers.Main) {
                addStringToTextView(getString(R.string.got_access_token))
            }
            delay(DURATION)
            val RiotVersion = getRiotClientVersion()
            withContext(Dispatchers.Main) {
                addStringToTextView(getString(R.string.got_riot_s_latest_version))
            }
            delay(DURATION)
            val entitlement = getEntitlement(accessToken, RiotVersion.first, RiotVersion.second)
            withContext(Dispatchers.Main) {
                if (entitlement == "Error")
                {
                    addStringToTextView(getString(R.string.error_getting_entitlement))
                    return@withContext
                }
                else{
                    addStringToTextView(getString(R.string.got_entitlement))
                }
            }
            delay(DURATION)
            val userInfo = getUserInfo(accessToken, RiotVersion.first, RiotVersion.second, entitlement, cookies)
            withContext(Dispatchers.Main) {
                if (userInfo == "Error")
                {
                    addStringToTextView(getString(R.string.error_getting_user_info))
                    return@withContext
                }
                else{
                    addStringToTextView(getString(R.string.decrypting_user))
                }
            }
            delay(DURATION)
            region = getUserRegion(accessToken, idToken)
            if (region == "Error")
            {
                withContext(Dispatchers.Main) {
                    addStringToTextView(getString(R.string.error_getting_region))
                    return@withContext
                }
            }
            else{
                withContext(Dispatchers.Main) {
                    addStringToTextView(getString(R.string.got_region))
                    addTranslations(region)
                }
            }
            val key = this@NewLogInUI.intent.getStringExtra("key")
            val puuid = userInfo.split(":")[0]
            val name = userInfo.split(":")[1]
            val tag = userInfo.split(":")[2]
            val intent = Intent(this@NewLogInUI, StaticsMainActivity::class.java)
            val riotPUUID = getRiotPUUID(name, tag)

            intent.putExtra("key", key)
            intent.putExtra("region", region)
            intent.putExtra("playerName", "$name#$tag")
            intent.putExtra("playerImageID", getPlayerImageRiot(riotPUUID, key!!))
            intent.putExtra("puuid", puuid)
            intent.putExtra("accessToken", accessToken)
            intent.putExtra("entitlement", entitlement)
            intent.putExtra("clientVersion", RiotVersion.first)
            intent.putExtra("clientPlatform", clientPlatformToken)
            intent.putExtra("cookies", cookies)
            intent.putExtra("idToken", idToken)
            intent.putExtra("riotPUUID", riotPUUID)

            delay(DURATION)
            withContext(Dispatchers.Main) {
                addStringToTextView(getString(R.string.hello) + name + " 👋")
            }
            delay(DURATION)
            withContext(Dispatchers.Main) {
                finish()
                startActivity(intent)
            }
        }
    }

    private fun addTranslations(region: String) {
        val translationsDB = ContentLocalisationDatabase(this)
        val url =
            "https://$region.api.riotgames.com/val/content/v1/contents?api_key=RGAPI-77322163-520c-492f-aabe-6c29a39f44ff"
        val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
        coroutineScope.launch {
            val dbVersion = translationsDB.getVersion()
            val json = JSONObject(URL(url).readText())
            val Gson = Gson().fromJson(json.toString(), Contents::class.java)
            if (dbVersion == "" || dbVersion != Gson.version) {
                translationsDB.addData(Gson.version, json.toString())
                withContext(Dispatchers.Main)
                {
                    Toast.makeText(
                        this@NewLogInUI,
                        "Asset Translations Updated",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun getUserInfo(
        accessToken: String,
        build: String,
        clientVersion: String,
        entitlement: String,
        cookies: String
    ): String {
        return runBlocking(Dispatchers.IO) {
            val client = OkHttpClient()
            val userInfoRequest = Request.Builder()
                .url("https://auth.riotgames.com/userinfo")
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $accessToken")
                .addHeader("X-Riot-ClientVersion", build)
                .addHeader("X-Riot-Entitlements-JWT", entitlement)
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                .addHeader("Cookie", cookies)
                .addHeader(
                    "User-Agent",
                    "RiotClient/$clientVersion rso-auth (Windows; 10;;Professional, x64)"
                )
                .get()
                .build()
            val userInfoResponse = client.newCall(userInfoRequest).execute()
            val code = userInfoResponse.code
            val body = userInfoResponse.body.string()

            if (code != 200)
            {
                return@runBlocking "Error"
            }
            val json = JSONObject(body)
            Log.d("RSO", "User info: $body")
            val sub =  json.getString("sub")

            // check if json has object "acct"
            if (!json.has("acct"))
            {
                return@runBlocking "Error"
            }
            val gameName = json.getJSONObject("acct").getString("game_name")
            val tagLine = json.getJSONObject("acct").getString("tag_line")
            return@runBlocking "$sub:$gameName:$tagLine"
        }
    }

    private fun getUserRegion(accessToken: String, id_token: String): String
    {
       return runBlocking(Dispatchers.IO) {
           val url = "https://riot-geo.pas.si.riotgames.com/pas/v1/product/valorant"

           val client = OkHttpClient()
           val body = """
            {
                "id_token": "$id_token"
            }
        """.trimIndent()
           val request = Request.Builder()
               .url(url)
               .addHeader("Content-Type", "application/json")
               .addHeader("Authorization", "Bearer $accessToken")
               .put(body.toRequestBody())
               .build()

           val response = client.newCall(request).execute()
           val code = response.code
           val bodyResponse = response.body.string()
           Log.d("RSO", "Region response: $bodyResponse")
           if (code != 200)
           {
               return@runBlocking "Error"
           }
           JSONObject(bodyResponse).getJSONObject("affinities").getString("live")
       }

    }

    private fun getRiotClientVersion() : Pair<String, String>
    {
        return runBlocking(Dispatchers.IO)
        {
            val JSON = JSONObject(URL("https://valorant-api.com/v1/version").readText())
            val version = JSON.getJSONObject("data").getString("riotClientBuild")
            val build = JSON.getJSONObject("data").getString("riotClientVersion")
            Pair(build, version)
        }
    }

    private fun getEntitlement(accessToken: String, build:String, clientVersion:String) : String
    {
        return runBlocking(Dispatchers.IO)
        {
            val client = okhttp3.OkHttpClient()
            val request = Request.Builder()
                .url("https://entitlements.auth.riotgames.com/api/token/v1")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Riot-ClientVersion", build)
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                .addHeader(
                    "User-Agent",
                    "RiotClient/$clientVersion rso-auth (Windows; 10;;Professional, x64)"
                )
                .addHeader("Authorization", "Bearer $accessToken")
                .post(
                    byteArrayOf().toRequestBody(null, 0, 0)
                ) // empty body
                .build()
            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body.string()
            if (code != 200) {
                return@runBlocking "Error"
            }

            Log.d("RSO", "Entitlements: $body")

            JSONObject(body).getString("entitlements_token")
        }
    }


    private fun getPlayerImageRiot(puuid: String, key: String): String {
        // run on main thread blocking
        return runBlocking(Dispatchers.IO) {
            try {
                val url =
                    "https://$region.api.riotgames.com/val/match/v1/matchlists/by-puuid/$puuid?api_key=$key"
                val json = JSONObject(URL(url).readText())
                val playerHistory = Gson().fromJson(json.toString(), MatchesByPUUID::class.java)
                val lastMatchID = playerHistory.history[0].matchId
                return@runBlocking getPlayerCardFromLastMatch(lastMatchID, key, puuid)
            } catch (e: Exception) {
                Log.d("PlayerImageError", e.message.toString())
                return@runBlocking "9fb348bc-41a0-91ad-8a3e-818035c4e561"
            }
        }
    }

    private fun getPlayerCardFromLastMatch(matchID: String, key: String, puuid: String): String {
        val url = "https://$region.api.riotgames.com/val/match/v1/matches/$matchID?api_key=$key"
        val json = JSONObject(URL(url).readText())
        val matchData = Gson().fromJson(json.toString(), ValorantMatch::class.java)

        for (player in matchData.players) {
            if (player.puuid == puuid) {
                return player.playerCard
            }
        }

        return "9fb348bc-41a0-91ad-8a3e-818035c4e561"
    }

    private fun getRiotPUUID(name: String, tag: String): String {
        val key = BuildConfig.RIOT_API_KEY
        val url =
            "https://europe.api.riotgames.com/riot/account/v1/accounts/by-riot-id/$name/$tag?api_key=$key"
        val client = OkHttpClient()
        return runBlocking(Dispatchers.IO) {
            val request = Request.Builder()
                .url(url)
                .get()
                .build()
            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body.string()
            if (code != 200)
            {
                return@runBlocking ""
            }
            val json = JSONObject(body)
            json.getString("puuid")
        }
    }
}
