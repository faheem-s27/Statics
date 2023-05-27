package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityNewLogInUiBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.net.URL
import javax.security.auth.Subject

class NewLogInUI : AppCompatActivity() {
    lateinit var binding : ActivityNewLogInUiBinding
    lateinit var webView : WebView
    lateinit var updateText: TextView
    private val clientPlatformToken =
        "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp9"


    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewLogInUiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        webView = binding.riotSignInWebVIEW
        updateText = binding.statusText

        webView.settings.javaScriptEnabled = true
        //val url = "https://auth.riotgames.com/authorize?client_id=statics&redirect_uri=https://statics-fd699.web.app/authorize.html&response_type=code&scope=openid+offline_access&prompt=login"
        val RiotURL = "https://auth.riotgames.com/authorize?redirect_uri=https%3A%2F%2Fplayvalorant.com%2Fopt_in&client_id=play-valorant-web-prod&response_type=token%20id_token&nonce=1"
        // track cookies
        CookieManager.getInstance().setAcceptCookie(true)
        webView.loadUrl(RiotURL)

        // Show a dialog to the user that says "Make sure to click on 'Stay signed in' when logging in"
        val dialog = AlertDialog.Builder(this)
        dialog.setTitle("Important")
        dialog.setMessage("Make sure to click on 'Stay signed in' when logging in")
        dialog.setPositiveButton("OK") { dialog, which ->
            dialog.dismiss()
        }
        dialog.show()

        webView.webViewClient = object : WebViewClient() {
            @Deprecated("Deprecated in Java")
            override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
                // check if the url contains the code
                if (url.contains("access_token=")) {
                    // get the cookies from the webview
                    val cookies = CookieManager.getInstance().getCookie(RiotURL)
                    val accessToken = url.split("access_token=")[1].split("&")[0]
                    authoriseUser(accessToken, cookies)
                }
                // else if the url doesn't contain the code, load the url
                else {
                    view.loadUrl(url)
                }
                return true
            }
        }
    }

    private fun addStringToTextView(text: String)
    {
        // animate the textview
        updateText.animate().alpha(0f).setDuration(100).withEndAction {
            updateText.text = updateText.text.toString() + "\n" + text
            updateText.animate().alpha(1f).setDuration(100).start()
        }.start()
    }

    fun authoriseUser(accessToken: String, cookies: String)
    {
        // hide the webview
        webView.alpha = 0f
        // make a loading progress dialog in the middle of the screen
        GlobalScope.launch {
            withContext(Dispatchers.Main)
            {
                addStringToTextView("Got cookies 🍪")
            }
            delay(200)
            withContext(Dispatchers.Main) {
                addStringToTextView("Got access token 🎫")
            }
            val RiotVersion = getRiotClientVersion()
            withContext(Dispatchers.Main) {
                addStringToTextView("Got Riot client version 📦")
            }
            val entitlement = getEntitlement(accessToken, RiotVersion.first, RiotVersion.second)
            withContext(Dispatchers.Main) {
                if (entitlement == "Error")
                {
                    addStringToTextView("Error getting entitlement ❌")
                    return@withContext
                }
                else{
                    addStringToTextView("Got entitlement 📜")
                }
            }
            val userInfo = getUserInfo(accessToken, RiotVersion.first, RiotVersion.second, entitlement, cookies)
            withContext(Dispatchers.Main) {
                if (userInfo == "Error")
                {
                    addStringToTextView("Error getting user info ❌")
                    return@withContext
                }
                else{
                    addStringToTextView("Got user info 👤")
                }
            }

            val partyTest = getPartyTest(accessToken, entitlement, userInfo, RiotVersion.first, RiotVersion.second)
        }
    }

    private fun getUserInfo(accessToken: String, build:String, clientVersion:String, entitlement: String, cookies: String): String
    {
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
            json.getString("sub")

            // check if json has object "acct"
//            if (!json.has("acct"))
//            {
//                return@runBlocking "Error"
//            }
//            val gameName = json.getJSONObject("acct").getString("game_name")
//            val tagLine = json.getJSONObject("acct").getString("tag_line")
//            return@runBlocking "$sub:$gameName:$tagLine"
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

    private fun getPartyTest(accessToken: String, entitlement: String, subject: String, build: String, clientVersion: String) : String
    {
        val client = OkHttpClient()
        val url = "https://pd.eu.a.pvp.net/personalization/v2/players/${subject}/playerloadout"
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Riot-Entitlements-JWT", entitlement)
            .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
            .addHeader("X-Riot-ClientVersion", build)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader(
                "User-Agent",
                "RiotClient/$clientVersion rso-auth (Windows; 10;;Professional, x64)"
            )
            .get()
            .build()

        return runBlocking(Dispatchers.IO) {
            val response = client.newCall(request).execute()
            val code = response.code
            val body = response.body.string()
            Log.d("RSO", "Party test: $body")
            if (code != 200) {
                return@runBlocking "Error"
            }
            return@runBlocking body
        }

    }

}