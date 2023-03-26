package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.LoggingInActivityRSO
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import okhttp3.FormBody
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LiveStatsFragment : Fragment() {
    lateinit var playerName: String
    lateinit var sharedPreferences: SharedPreferences
    var bearerToken: String? = null
    var accessToken: String? = null
    var refreshToken: String? = null
    var idToken: String? = null
    var entitlementToken: String? = null
    var playerId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_live_stats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        playerName = activity?.intent?.getStringExtra("playerName") ?: return
        sharedPreferences = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)

        val liveButtonCheck = view.findViewById<View>(R.id.button)
        liveButtonCheck.setOnClickListener {
            getAuth()
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

    @OptIn(DelicateCoroutinesApi::class)
    private fun getAuth() {
        // Check for bearerToken, accessToken, refreshToken, idToken in SharedPreferences
        bearerToken = sharedPreferences.getString("bearerToken", null)
        accessToken = sharedPreferences.getString("accessToken", null)
        refreshToken = sharedPreferences.getString("refreshToken", null)
        idToken = sharedPreferences.getString("idToken", null)

        // If any of the tokens are null, then the user is not logged in
        if (bearerToken == null || accessToken == null || refreshToken == null || idToken == null) {
            // show dialog to login again
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Login Required!")
            dialog.setMessage("You need to login again to access this feature.")
            dialog.setPositiveButton("Log out") { _, _ ->
                logOut(playerName.split("#")[0])
            }
            dialog.setNegativeButton("Cancel") { _, _ ->
            }
            dialog.show()
            return
        }

        entitlementToken = sharedPreferences.getString("entitlementToken", null)
        if (entitlementToken == null) {
            GlobalScope.launch {
                getUserInfo(accessToken)
                val entitlementToken = getEntitlementToken(accessToken)
                if (entitlementToken != null) {
                    //sharedPreferences.edit().putString("entitlementToken", entitlementToken).apply()
                }
            }
        }

    }

    private fun getUserInfo(accessToken: String?) {
        val client = OkHttpClient()
        val urlBuilder = "https://auth.riotgames.com/userinfo".toHttpUrlOrNull()?.newBuilder()
        val url = urlBuilder?.build().toString()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseString = response.body.string()

        if (responseString != null) {
            if (responseString == "\"\"") {
                Log.d("LIVE_STATS_UserInfo", "Empty response, refresh token!")
                refreshToken()
                return
            }
            // get player id from "sub" asjson
            val json = JSONObject(responseString)
            playerId = json.getString("sub")
            Log.d("LIVE_STATS_UserInfo", "The player id is $playerId")
        }
    }

    private fun refreshToken() {
        Log.d("LIVE_STATS_RefreshToken", "Refreshing token...")
        if (refreshToken == null || accessToken == null || bearerToken == null) {
            return
        }

        val client = OkHttpClient()
        val urlBuilder = " https://auth.riotgames.com/token".toHttpUrlOrNull()?.newBuilder()
        val url = urlBuilder?.build().toString()

        val formBody: RequestBody = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", refreshToken!!)
            .build()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + bearerToken!!)
            .post(formBody)
            .build()

        val response = client.newCall(request).execute()
        val responseString = response.body.string()
        Log.d("LIVE_STATS_RefreshToken", responseString!!)
    }

    private fun getEntitlementToken(accessToken: String?): String? {
        if (accessToken == null) {
            return null
        }
        val client = OkHttpClient()

        val authURL = "https://auth.riotgames.com/api/v1/authorization"
        var authBody =
            "{\"client_id\":\"play-valorant-web-prod\",\"nonce\":\"1\",\"redirect_uri\":\"https://playvalorant.com/opt_in\",\"response_type\":\"token id_token\",\"scope\":\"account openid\"}"
        // make user agent as if it was a PC browser
        val userAgent =
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.116 Safari/537.36"
        val authRequest = Request.Builder()
            .url(authURL)
            .addHeader("User-Agent", userAgent)
            .addHeader("Content-Type", "application/json")
            .post(authBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        val authResponse = client.newCall(authRequest).execute()
        val authHeaders = authResponse.headers
        val authResponseString = authResponse.body.string()
        Log.d("LIVE_STATS_EntitlementToken", authResponseString!!)
        Log.d("LIVE_STATS_EntitlementToken", authHeaders.toString())

        authBody =
            "{\"type\":\"auth\",\"username\":\"pinchedrainbow\",\"password\":\"Anayamiral2004\"}"
        val authRequest2 = Request.Builder()
            .url(authURL)
            .addHeader("Content-Type", "application/json")
            .post(authBody.toRequestBody("application/json".toMediaTypeOrNull()))
            .build()

        val authResponse2 = client.newCall(authRequest2).execute()
        val authResponseString2 = authResponse2.body.string()
        Log.d("LIVE_STATS_EntitlementToken", authResponseString2!!)
//
//

        val urlBuilder =
            "https://entitlements.auth.riotgames.com/api/token/v1".toHttpUrlOrNull()?.newBuilder()
        val url = urlBuilder?.build().toString()

        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .addHeader("Content-Type", "application/json")
            .build()

        val response = client.newCall(request).execute()
        val responseString = response.body.string()
        if (responseString != null) {
            Log.d("LIVE_STATS_EntitlementToken", responseString)
            return responseString
        }
        return null

    }

}
