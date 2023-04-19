package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.AlertDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.BaseAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.RelativeLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.main.LoadingActivity
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL
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
        region = activity?.intent?.getStringExtra("region") ?: return
        authPreferences = requireActivity().getSharedPreferences("auth", Context.MODE_PRIVATE)

        shard =
            if (region.lowercase(Locale.ROOT) == "latam" || region.lowercase(Locale.getDefault()) == "br") {
                "na"
            } else {
                region.lowercase(Locale.getDefault())
            }

        INITVIEW = requireView().findViewById(R.id.InitView)
        LIVEVIEW = requireView().findViewById(R.id.LiveView)

        agentPreGameRecyclerView = requireView().findViewById(R.id.new_agentSelectGridRecyclerView)
        agentPreGameRecyclerView.layoutManager = GridLayoutManager(requireContext(), 6)

        INITVIEW.visibility = View.VISIBLE
        LIVEVIEW.visibility = View.GONE

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

        GlobalScope.launch {
            val url = "https://valorant-api.com/v1/version"
            val json = JSONObject(URL(url).readText())
            ClientVersion = json.getJSONObject("data").getString("riotClientBuild")
            riotClientVersion = json.getJSONObject("data").getString("riotClientVersion")

            val spraysURL = "https://valorant-api.com/v1/sprays"
            val spraysJSON = JSONObject(URL(spraysURL).readText())
            val spraysData = spraysJSON.getJSONArray("data")
            for (i in 0 until spraysData.length()) {
                val spray = spraysData.getJSONObject(i)
                SpraysIDImage[spray.getString("uuid")] = spray.getString("fullTransparentIcon")
            }

            val agentsURL = "https://valorant-api.com/v1/agents?isPlayableCharacter=true"
            val agentsJSON = JSONObject(URL(agentsURL).readText())
            val agentsData = agentsJSON.getJSONArray("data")
            for (i in 0 until agentsData.length()) {
                val agent = agentsData.getJSONObject(i)
                AgentNamesID[agent.getString("uuid")] = agent.getString("displayName")
            }

            val mapsURL = "https://valorant-api.com/v1/maps"
            val mapsJSON = JSONObject(URL(mapsURL).readText())
            val mapsData = mapsJSON.getJSONArray("data")
            for (i in 0 until mapsData.length()) {
                val map = mapsData.getJSONObject(i)
                MapsImagesID[map.getString("mapUrl")] = map.getString("splash")
            }

            val gamePodURL = "https://valorant-api.com/internal/locres/en-US"
            val gamePodJSON = JSONObject(URL(gamePodURL).readText())
            val gamePodData = gamePodJSON.getJSONObject("data")
            val gamePodUIStrings = gamePodData.getJSONObject("UI_GamePodStrings")
            // for each object in the game pod strings get the key and value and add it to the hashmap
            for (key in gamePodUIStrings.keys()) {
                GamePodStrings[key] = gamePodUIStrings.getString(key)
                Log.d("LIVE_STATS_GAMEPODS", "$key: ${gamePodUIStrings.getString(key)}")
            }
        }

        val username = authPreferences.getString("username", null)
        val password = authPreferences.getString("password", null)

        val usernamepassword: Button = requireView().findViewById(R.id.continueInit2)
        usernamepassword.text = "Get started"
        usernamepassword.setOnClickListener {
            if (username != null && password != null) {
                authenticateUser(username, password)
            } else {
                loadUI("INIT")
            }
        }

        val whyPassword = requireView().findViewById<Button>(R.id.continueInit)
        whyPassword.setOnClickListener {
            val builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
                .setTitle("Why do I need to enter my password?")
                .setMessage(
                    "Unfortunately, Statics needs your Riot username and password to use the live mode as the password is required to authenticate your account and is used to get the data from Valorant.\n\nThe password is not stored anywhere and is only used to authenticate your account." +
                            "\n\nI won't sugarcoat things. Due to the way Statics uses the API, I believe there's no other way to sign in than to have people enter their credentials directly. \n" +
                            "Under those circumstances, there's really no good way for anyone to be sure their credentials remain safe. \n" +
                            "\nFor what it's worth:\n" +
                            "• Your credentials only ever leave your device in the form of a login request directly to Riot. They are never stored or sent anywhere else.\n" +
                            "• You can enable 2-factor authentication, so you'd at least have another layer of security if it went bad.\n" +
                            "\n" +
                            "The code is open-source and visible on GitHub (pending); you can build it yourself if you want to be sure of what's running.\n" +
                            "TL;DR: you'll have to take my word for it. \uD83E\uDD86❤️ " + "\n\n" + "If you're still not comfortable with this, you can use the app without live mode."
                )
                .setPositiveButton("OK", null)

            builder.show()
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

    private fun INITMode() {
        val usernamepassword: Button = requireView().findViewById(R.id.continueInit2)
        usernamepassword.text = "Username/Password"
        usernamepassword.setOnClickListener {
            val dialogView = LayoutInflater.from(requireActivity())
                .inflate(R.layout.dialog_username_password, null)

            val editTextUsername = dialogView.findViewById<EditText>(R.id.editTextUsername)
            val editTextPassword = dialogView.findViewById<EditText>(R.id.editTextPassword)

            // set a listener on the username to detect the character # and remove it
            editTextUsername.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                }

                override fun afterTextChanged(s: Editable?) {
                    if (s.toString().contains("#")) {
                        editTextUsername.setText(s.toString().replace("#", ""))
                        editTextUsername.setSelection(editTextUsername.text.length)
                        // show a toast to tell the user that the character # is not allowed
                        Toast.makeText(
                            requireContext(),
                            "The character # is not allowed",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            })

            val builder = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
                .setView(dialogView)
                .setTitle("Enter username and password")
                .setPositiveButton("OK") { _, _ ->
                    val username = editTextUsername.text.toString()
                    val password = editTextPassword.text.toString()

                    if (username.isEmpty() || password.isEmpty()) {
                        Toast.makeText(
                            requireContext(),
                            "Please enter a username and password",
                            Toast.LENGTH_SHORT
                        ).show()
                        return@setPositiveButton
                    }

                    // hide the keyboard
                    val imm =
                        requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(requireView().windowToken, 0)

                    // Do something with the username and password
                    // base 64 encode the password
                    val passwordBase64 =
                        Base64.encodeToString(password.toByteArray(), Base64.DEFAULT)
                    authenticateUser(username, passwordBase64)
                }
                .setNegativeButton("Cancel", null)
            builder.show()
        }
    }

    private fun authenticateUser(username: String, password: String) {
        // show the Statics Dialog Progress Bar
        val progressdialog =
            ProgressDialogStatics().setProgressDialog(requireActivity(), "Authenticating...")
        progressdialog.show()

        // create a new coroutine scope
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            try {
                val mediaType = "application/json".toMediaTypeOrNull()
                val authBody = AuthCookiesBody()
                val authBodyJson = Gson().toJson(authBody).toRequestBody(mediaType)

                // create a new request
                val request = Request.Builder()
                    .url("https://auth.riotgames.com/api/v1/authorization")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                    .post(authBodyJson)
                    .build()

                // execute the request
                val response = client.newCall(request).execute()
                val code = response.code
                val body = response.body.string()

                if (response.code != 200) {
                    withContext(Dispatchers.Main)
                    {
                        progressdialog.dismiss()
                        val msg = "Response code: $code\nBody: $body"
                        val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                            .setTitle("Response from Auth Cookies")
                            .setMessage(msg)
                            .setPositiveButton("OK") { dialog, _ ->
                                // clear username and password from shared preferences
                                authPreferences.edit().remove("username").apply()
                                authPreferences.edit().remove("password").apply()

                                // clear the cookies from shared preferences
                                authPreferences.edit().remove("cookieMFA").apply()
                                authPreferences.edit().remove("cookieAuth").apply()
                                dialog.dismiss()
                            }
                        dialog.show()
                    }

                    return@launch
                }

                // get the cookie
                val cookies = response.headers("Set-Cookie")
                var cookieHeader = TextUtils.join("; ", cookies)

                // get the mfaCookie if it exists in shared preferences
                val mfaCookie = authPreferences.getString("cookieMFA", null)
                if (mfaCookie != null) {
                    // add the mfa cookie to the cookie header
                    cookieHeader += "; $mfaCookie"
                }

                // get the auth cookie if it exists in shared preferences
                val authCookie = authPreferences.getString("cookieAuth", null)
                if (authCookie != null) {
                    // add the auth cookie to the cookie header
                    cookieHeader += "; $authCookie"
                }

                // decode the base64 password
                val passwordDecoded = String(Base64.decode(password, Base64.DEFAULT))

                val authBodyUserNamePassword = AuthRequestBody(username, passwordDecoded)
                val authBodyUserNamePasswordJson =
                    Gson().toJson(authBodyUserNamePassword).toRequestBody(mediaType)

                val authRequest = Request.Builder()
                    .url("https://auth.riotgames.com/api/v1/authorization")
                    .addHeader("Content-Type", "application/json")
                    .addHeader("Accept", "application/json")
                    .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                    .addHeader("Cookie", cookieHeader)
                    .put(authBodyUserNamePasswordJson)
                    .build()

                // execute the request
                val authResponse = client.newCall(authRequest).execute()
                // get the response body
                val authResponseBody = authResponse.body.string()

                val authCookies = authResponse.headers("Set-Cookie")
                val authCookieHeader = TextUtils.join("; ", authCookies)

                val jsonAuthBody = JSONObject(authResponseBody)
                // look for error in the response body
                val error = jsonAuthBody.optString("error", "")
                if (error.isNotEmpty()) {
                    withContext(Dispatchers.Main)
                    {
                        if (jsonAuthBody.getString("error") == "auth_failure") {
                            progressdialog.dismiss()
                            val msg =
                                "Response was successful but username or password is incorrect"
                            val dialog =
                                AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                                    .setTitle("Response from Statics")
                                    .setMessage(msg)
                                    .setPositiveButton("OK") { _, _ ->
                                        // clear the username and password from authPrefs
                                        authPreferences.edit().remove("username").apply()
                                        authPreferences.edit().remove("password").apply()

                                        // clear the cookies from shared preferences
                                        authPreferences.edit().remove("cookieMFA").apply()
                                        authPreferences.edit().remove("cookieAuth").apply()

                                    // dismiss the dialog
                                    progressdialog.dismiss()

                                    val intent =
                                        Intent(requireContext(), LoadingActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()
                                    activity?.overridePendingTransition(
                                        R.anim.fadein,
                                        R.anim.fadeout
                                    )
                                }
                            dialog.show()
                        } else {
                            progressdialog.dismiss()
                            val msg =
                                "An error occurred when signing in\n\n\nError message: $jsonAuthBody"
                            val dialog = AlertDialog.Builder(requireContext())
                                .setTitle("Response from Statics")
                                .setMessage(msg)
                                .setPositiveButton("OK") { _, _ ->
                                    // clear the username and password from authPrefs
                                    authPreferences.edit().remove("username").apply()
                                    authPreferences.edit().remove("password").apply()

                                    // clear the cookies from shared preferences
                                    authPreferences.edit().remove("cookieMFA").apply()
                                    authPreferences.edit().remove("cookieAuth").apply()

                                    // dismiss the dialog
                                    progressdialog.dismiss()

                                    val intent =
                                        Intent(requireContext(), LoadingActivity::class.java)
                                    startActivity(intent)
                                    activity?.finish()
                                    activity?.overridePendingTransition(
                                        R.anim.fadein,
                                        R.anim.fadeout
                                    )
                                }
                            dialog.show()
                        }
                    }
                    return@launch
                }

                withContext(Dispatchers.Main)
                {
                    progressdialog.dismiss()
                    // store authCookieHeader in shared preferences
                    //authPreferences.edit().putString("cookieAuth", authCookieHeader).apply()
                    storeEverything(username, password, authResponseBody, authCookieHeader)
                }
            } catch (e: java.lang.Exception) {
                withContext(Dispatchers.Main)
                {
                    progressdialog.dismiss()
                    val msg = "An error occurred\n\n\nError message: $e"
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Response from Statics")
                        .setMessage(msg)
                        .setPositiveButton("OK") { _, _ ->
                            // clear the username and password from authPrefs
                            authPreferences.edit().remove("username").apply()
                            authPreferences.edit().remove("password").apply()

                            // clear the cookies from shared preferences
                            authPreferences.edit().remove("cookieMFA").apply()
                            authPreferences.edit().remove("cookieAuth").apply()

                            // dismiss the dialog
                            progressdialog.dismiss()

                            val intent = Intent(requireContext(), LoadingActivity::class.java)
                            startActivity(intent)
                            activity?.finish()
                            activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                        }
                    dialog.show()
                }
            }
        }

    }

    private fun reauthCookies(): Boolean {
        val authCookie = authPreferences.getString("cookieAuth", null) ?: return false

        val url =
            "https://auth.riotgames.com/authorize?redirect_uri=https%3A%2F%2Fplayvalorant.com%2Fopt_in&client_id=play-valorant-web-prod&response_type=token%20id_token&nonce=1"
        val request = Request.Builder()
            .url(url)
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
            .addHeader("Cookie", authCookie)
            .build()

        return runBlocking(Dispatchers.IO) {
            val authResponse = client.newCall(request).execute()
            val authCookies = authResponse.headers("Set-Cookie")
            val authCookieHeader = TextUtils.join("; ", authCookies)
            val json = JSONObject(authResponse.body.string())
            val response = json.optJSONObject("response")
            if (response != null) {
                val uri = response.getJSONObject("parameters").getString("uri")
                // get value of access_token from query by searching for access_token
                val access = uri.substringAfter("access_token=").substringBefore("&")
                // store cookie in shared preferences
                authPreferences.edit().putString("cookieAuth", authCookieHeader).apply()
                val entitlementResponse = refreshEntitlement(access)
                val entitlementResponseCode = entitlementResponse.code
                val entitlementResponseBody = entitlementResponse.body.string()
                if (entitlementResponseCode != 200) {
                    return@runBlocking false
                }
                val entitlementJSON = JSONObject(entitlementResponseBody)
                val Etoken = entitlementJSON.getString("entitlements_token")
                accessToken = access
                entitlementToken = Etoken
                return@runBlocking true
            } else {
                val type = json.getString("type")
                val email = json.getJSONObject("multifactor").getString("email")
                if (type == "multifactor") {
                    val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                    val inflater = LayoutInflater.from(context)
                    val dialogView = inflater.inflate(R.layout.dialog_multifactor, null)
                    builder.setView(dialogView)

                    // Add any other dialog box configuration here (e.g. title, buttons)
                    builder.setTitle("2FA Code for $email")
                    builder.setPositiveButton("Send") { _, _ ->
                        val code =
                            dialogView.findViewById<EditText>(R.id.multifactor_code_input).text.toString()
                        send2FAcode(code, authCookieHeader, true)
                    }

                    val dialog = builder.create()
                    dialog.show()
                }
            }
            return@runBlocking true
        }
    }

    private fun storeEverything(
        username: String,
        password: String,
        uriParam: String,
        cookieHeader: String
    ) {
        // store the username and password in authPrefs

        authPreferences.edit().putString("username", username).apply()
        authPreferences.edit().putString("password", password).apply()

        val json = JSONObject(uriParam)

        // look for response json object
        val response = json.optJSONObject("response")
        if (response != null) {
            val uri = response.getJSONObject("parameters").getString("uri")
            // get value of access_token from query by searching for access_token
            val access = uri.substringAfter("access_token=").substringBefore("&")
            getEntitlement(access)
        } else {
            val type = json.getString("type")
            val email = json.getJSONObject("multifactor").getString("email")
            if (type == "multifactor") {
                val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
                val inflater = LayoutInflater.from(context)
                val dialogView = inflater.inflate(R.layout.dialog_multifactor, null)
                builder.setView(dialogView)

                // Add any other dialog box configuration here (e.g. title, buttons)
                builder.setTitle("2FA Code for $email")
                builder.setPositiveButton("Send") { _, _ ->
                    val code =
                        dialogView.findViewById<EditText>(R.id.multifactor_code_input).text.toString()
                    send2FAcode(code, cookieHeader)
                }

                val dialog = builder.create()
                dialog.show()
            }
        }
    }

    private fun send2FAcode(code: String, cookieHeader: String, refresh: Boolean? = false) {
        val multifactorRequest = Request.Builder()
            .url("https://auth.riotgames.com/api/v1/authorization")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("X-Riot-ClientVersion", riotClientVersion)
            .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
            .addHeader(
                "User-Agent",
                "RiotClient/$ClientVersion rso-auth (Windows; 10;;Professional, x64)"
            )
            .addHeader("Cookie", cookieHeader)
            .put(
                """
                            {
                                "type": "multifactor",
                                "code": "$code",
                                "rememberDevice": true
                            }
                        """.trimIndent().toRequestBody("application/json".toMediaTypeOrNull())
            )
            .build()

        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val responseMFA = client.newCall(multifactorRequest).execute()
            val body = responseMFA.body.string()
            // Log the response code and body
            withContext(Dispatchers.Main)
            {
                if (responseMFA.code != 200) {
                    val msg = "Response code: $code\nBody: $body"
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Response from Statics 2FA")
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                    dialog.show()
                } else {
                    val json = JSONObject(body)
                    val error = json.optString("error", "")
                    if (error.isNotEmpty()) {
                        val msg = "Incorrect 2FA code"
                        val dialog = AlertDialog.Builder(requireContext())
                            .setTitle("Response from Statics")
                            .setMessage(msg)
                            .setPositiveButton("OK", null)
                        dialog.show()
                        return@withContext
                    }

                    val mfaCookies = responseMFA.headers("Set-Cookie")
                    val mfaCookieHeader = TextUtils.join("; ", mfaCookies)
                    // save the cookie
                    authPreferences.edit().putString("cookieMFA", mfaCookieHeader)
                        .apply()

                    val uri = json.getJSONObject("response").getJSONObject("parameters")
                        .getString("uri")
                    val access =
                        uri.substringAfter("access_token=").substringBefore("&")
                    if (refresh == true) {
                        val response = refreshEntitlement(access)
                    } else {
                        getEntitlement(access)
                    }
                }
            }
        }
    }

    private fun refreshEntitlement(newAccess: String): Response {
        val request = Request.Builder()
            .url("https://entitlements.auth.riotgames.com/api/token/v1")
            .addHeader("Content-Type", "application/json")
            .addHeader("X-Riot-ClientVersion", riotClientVersion)
            .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
            .addHeader(
                "User-Agent",
                "RiotClient/$ClientVersion rso-auth (Windows; 10;;Professional, x64)"
            )
            .addHeader("Authorization", "Bearer $newAccess")
            .post(
                byteArrayOf().toRequestBody(null, 0, 0)
            ) // empty body
            .build()

        return runBlocking(Dispatchers.IO) {
            return@runBlocking client.newCall(request).execute()
        }
    }

    private fun getEntitlement(access: String) {
        GlobalScope.launch {
            val request = Request.Builder()
                .url("https://entitlements.auth.riotgames.com/api/token/v1")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Riot-ClientVersion", riotClientVersion)
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                .addHeader(
                    "User-Agent",
                    "RiotClient/$ClientVersion rso-auth (Windows; 10;;Professional, x64)"
                )
                .addHeader("Authorization", "Bearer $access")
                .post(
                    byteArrayOf().toRequestBody(null, 0, 0)
                ) // empty body
                .build()

            val response = client.newCall(request).execute()
            val code = response.code

            if (response.code != 200) {
                withContext(Dispatchers.Main)
                {
                    val body = response.body.string()
                    val msg = "Response code: $code\nBody: $body"
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Response from Entitlement Token")
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                    dialog.show()
                }
                return@launch
            }

            val entitlementResponse = response.body.string()
            val entitlementJSON = JSONObject(entitlementResponse)
            val Etoken = entitlementJSON.getString("entitlements_token")
            accessToken = access
            entitlementToken = Etoken

            val userInfoRequest = Request.Builder()
                .url("https://auth.riotgames.com/userinfo")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                .addHeader("Authorization", "Bearer $access")
                .addHeader("X-Riot-ClientVersion", riotClientVersion)
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
                .addHeader(
                    "User-Agent",
                    "RiotClient/$ClientVersion rso-auth (Windows; 10;;Professional, x64)"
                )
                .build()

            val userInfoResponse = client.newCall(userInfoRequest).execute()
            val userInfoResponseCode = userInfoResponse.code

            if (userInfoResponseCode != 200) {
                withContext(Dispatchers.Main)
                {
                    val body = userInfoResponse.body.string()
                    val msg = "Response code: $userInfoResponseCode\nBody: $body"
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Response from User Info")
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                    dialog.show()
                }
                return@launch
            }

            val userInfoResponseBody = userInfoResponse.body.string()
            val userInfoJSON = JSONObject(userInfoResponseBody)
            val userID = userInfoJSON.getString("sub")

            PlayerUUID = userID

            Log.d("LIVE_STATS", "UserInfo: $userInfoJSON")

            val gameName = userInfoJSON.getJSONObject("acct").getString("game_name")
            val tagLine = userInfoJSON.getJSONObject("acct").getString("tag_line")

            val partyPlayerNameTV = requireView().findViewById<TextView>(R.id.new_partyPlayerName)
            val partyPlayerTagTV = requireView().findViewById<TextView>(R.id.new_partyPlayerTag)
            partyPlayerNameTV.text = gameName
            partyPlayerTagTV.text = "#$tagLine"

            withContext(Dispatchers.Main)
            {
                loadUI("LIVE")
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
            timerLiveAPI()
            getPlayerLoadOuts()

            // Throw an exception as a test
//            throw Exception("Hello Discord, this is a test so that next time you get an error\n\nYou do the following:\n\n1. Click on 'Copy & Send'\n2. Send it in channel 'bugs and issues'" +
//                    "\n\nBoom! I'll now be able to help out much more!")

        } catch (e: Exception) {
            // Alert the user and ask to send a bug report
            val msg = "An error happened in live mode: ${e.message}"
            val dialog = AlertDialog.Builder(requireContext(), R.style.AlertDialogTheme)
                .setTitle("Send to developer on discord!")
                .setMessage(msg)
                .setPositiveButton("Copy & Send") { dialog, which ->
                    // copy to clipboard and send to discord
                    val clipboard =
                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("Valorant Live Stats", msg)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(requireContext(), "Copied to clipboard!", Toast.LENGTH_SHORT)
                        .show()
                    val intent = Intent(Intent.ACTION_VIEW)
                    intent.data = Uri.parse("https://discord.gg/jwfJUQMPP7")
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
            dialog.show()
        }
    }

    // a kotlin coroutine to get the party status every second
    private fun timerLiveAPI() {
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
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

    private fun getContracts() {
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
        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code

        if (code != 200) return

        Log.d("LIVE_STATS_STOREFRONT", "Response code: $code Body: $body")

        val json = JSONObject(body)
        // see if theres a object called BonusStore
        val timerSeconds =
            json.getJSONObject("FeaturedBundle").getInt("BundleRemainingDurationInSeconds")
        setStoreTimer(timerSeconds)
        val NightMarket = json.optJSONObject("BonusStore")
        if (NightMarket != null) {
            // show dialog
            //Snackbar.make(requireView(), "Night Market is open!", Snackbar.LENGTH_LONG).show()
        }
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
        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code

        if (code == 200) {
            // show dialog
            val json = JSONObject(body)
            loadSprays(json.getJSONArray("Sprays"))
            loadTitle(json.getJSONObject("Identity").getString("PlayerTitleID"))
            loadPlayerCard(json.getJSONObject("Identity").getString("PlayerCardID"))
        }

        showStoreFront()
        getContracts()
    }

    private fun loadPlayerCard(titleID: String) {
        val largeURL = "https://media.valorant-api.com/playercards/${titleID}/largeart.png"
        val smallURL = "https://media.valorant-api.com/playercards/${titleID}/smallart.png"

        val smolImg = view?.findViewById<ImageView>(R.id.new_playerAvatar)
        val bigImg = view?.findViewById<ImageView>(R.id.new_LiveStatsBackground)

        Picasso.get().load(smallURL).fit().centerCrop().into(smolImg)
        Picasso.get().load(largeURL).fit().centerCrop().into(bigImg)
    }

    private fun updatePlayerCard(selectedPicture: String) {
        val url =
            "https://pd.${shard}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
        val response = APIRequestValorant(url)
        var body = response.body.string()
        val code = response.code

        if (code != 200) return

        val json = JSONObject(body)
        val identity = json.getJSONObject("Identity")
        identity.put("PlayerCardID", selectedPicture)

        // convert to string
        body = json.toString()

        val sprayResponse = APIRequestValorant(url, body, true)
        val sprayBody = sprayResponse.body.string()
        val sprayCode = sprayResponse.code

        if (sprayCode == 200) {
            Snackbar.make(
                requireView(),
                "Card updated successfully",
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

    private fun loadTitle(TitleID: String) {
        val titleName = assetsDB.retrieveName(TitleID)
        view?.findViewById<TextView>(R.id.CurrentLoadoutTitle)?.text = titleName

        view?.findViewById<ImageView>(R.id.CurrentLoadoutTitleEdit)?.setOnClickListener {
            // get all of the titles from the assets db
            val titles = getAvailableTitles()
            // convert to CharSequence
            val titlesCharSequence = titles.map { it }.toTypedArray()
            // show a dialog with all of the titles
            val dialog = AlertDialog.Builder(requireContext())
                .setTitle("Select a Title")
                .setItems(titlesCharSequence) { _, which ->
                    // get the title id from the selected item
                    val titleID = assetsDB.retrieveIDTitle(titles[which])
                    // update the title
                    updateTitle(titleID)
                }
                .setNegativeButton("Cancel", null)

            // set the items text color to white
            val dialogView = dialog.create()
            dialogView.show()
            dialogView.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE)
            dialogView.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE)
        }
    }

    private fun updateTitle(titleID: String) {
        val url =
            "https://pd.${shard}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
        val response = APIRequestValorant(url)
        var body = response.body.string()
        val code = response.code

        if (code != 200) return

        val json = JSONObject(body)
        val identity = json.getJSONObject("Identity")
        identity.put("PlayerTitleID", titleID)

        // convert to string
        body = json.toString()

        val titleResponse = APIRequestValorant(url, body, true)
        val titleBody = titleResponse.body.string()
        val titleCode = titleResponse.code

        if (titleCode == 200) {
            // update the title
            //loadTitle(titleID)
            Snackbar.make(
                requireView(),
                "Title updated successfully",
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

    private fun updateSpray(sprayID: String, sprayEquipID: String) {
        val url =
            "https://pd.${shard}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
        val response = APIRequestValorant(url)
        var body = response.body.string()
        val code = response.code

        if (code != 200) return

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

        if (sprayCode == 200) {
            Snackbar.make(
                requireView(),
                "Spray updated successfully",
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
    private fun loadSprays(Sprays: JSONArray) {
        val PreRoundSprayView = view?.findViewById<ImageView>(R.id.CurrentLoadoutSprayPreRound)
        val MidRoundSprayView = view?.findViewById<ImageView>(R.id.CurrentLoadoutSprayMidRound)
        val PostRoundSprayView = view?.findViewById<ImageView>(R.id.CurrentLoadoutSprayPostRound)

        val PreRoundID = "0814b2fe-4512-60a4-5288-1fbdcec6ca48"
        val MidRoundID = "04af080a-4071-487b-61c0-5b9c0cfaac74"
        val PostRoundID = "5863985e-43ac-b05d-cb2d-139e72970014"

        for (i in 0 until Sprays.length()) {
            val EquipSlotID = Sprays.getJSONObject(i).getString("EquipSlotID")
            when (EquipSlotID) {
                PreRoundID -> {
                    // Pre Round Spray
                    Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(i).getString("SprayID")])
                        .fit()
                        .centerInside().into(PreRoundSprayView)
                }

                MidRoundID -> {
                    // Mid Round Spray
                    Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(i).getString("SprayID")])
                        .fit()
                        .centerInside().into(MidRoundSprayView)
                }

                PostRoundID -> {
                    // Post Round Spray
                    Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(i).getString("SprayID")])
                        .fit()
                        .centerInside().into(PostRoundSprayView)
                }
            }
        }

        val sprays = getAvailableSprays()
        var alertDialog: AlertDialog = AlertDialog.Builder(requireActivity()).create()
        val dialogView =
            LayoutInflater.from(requireActivity()).inflate(R.layout.dialog_picture_list, null)
        val listViewPictures = dialogView.findViewById<ListView>(R.id.listViewPictures)
        listViewPictures.adapter = PictureListAdapter(
            requireActivity(),
            sprays,
            "Spray"
        ) // Create an adapter for the list of pictures

        alertDialog = AlertDialog.Builder(requireActivity(), R.style.AlertDialogTheme)
            .setTitle("Select a Spray")
            .setView(dialogView)
            .setNegativeButton("Cancel") { _, _ ->
                // Handle cancel button click event, if needed
            }
            .create()
        PreRoundSprayView!!.setOnClickListener {
            listViewPictures.setOnItemClickListener { _, _, position, _ ->
                val selectedPicture = sprays[position]
                updateSpray(selectedPicture, PreRoundID)
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        MidRoundSprayView!!.setOnClickListener {
            listViewPictures.setOnItemClickListener { _, _, position, _ ->
                val selectedPicture = sprays[position]
                updateSpray(selectedPicture, MidRoundID)
                alertDialog.dismiss()
            }

            alertDialog.show()
        }

        PostRoundSprayView!!.setOnClickListener {
            listViewPictures.setOnItemClickListener { _, _, position, _ ->
                val selectedPicture = sprays[position]
                updateSpray(selectedPicture, PostRoundID)
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
                .setTitle("Log out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    // log out
                    // clear the shared preferences from auth
                    authPreferences.edit().clear().apply()
                    // go to loading screen
                    val intent = Intent(requireContext(), LoadingActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                    activity?.overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }
                .setNegativeButton("No", null)
            dialog.show()
        }

        val avatarPFP = view?.findViewById<ImageView>(R.id.new_playerAvatar)
        avatarPFP?.setOnClickListener {
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
                .setTitle("Select a Player Card")
                .setView(dialogView)
                .setNegativeButton("Cancel") { _, _ ->
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

        val readySwitch = requireView().findViewById<MaterialSwitch>(R.id.new_readySwitch)
        readySwitch?.setOnClickListener {
            val ready = readySwitch.isChecked
            handleMemberReady(ready)
        }
    }

    private fun handleMemberReady(ready: Boolean) {
        val readySwitch = requireView().findViewById<MaterialSwitch>(R.id.new_readySwitch)
        if (playerPartyID == null) return
        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${playerPartyID}/members/${PlayerUUID}/setReady"
        val body = "{\"ready\": $ready}"
        val response = APIRequestValorant(url, body)
        if (response.code == 200) {
            readySwitch?.isChecked = ready
        } else {
            readySwitch?.isChecked = !ready
        }
    }

    private fun joinMatchmaking() {
        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${playerPartyID}/matchmaking/join"
        val response = APIRequestValorant(url, "")
        val code = response.code
        if (code == 200) {
            val partyStatus = view?.findViewById<TextView>(R.id.new_playerPartyStatus)
            partyStatus?.text = "Matchmaking..."
            val joinMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
            joinMatchButton?.text = "Cancel queue"
        }
    }

    private fun cancelMatchmaking() {
        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${playerPartyID}/matchmaking/leave"
        APIRequestValorant(url, "")
    }

    private fun getPartyStatus() {
        try {
            val response =
                APIRequestValorant("https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/players/${PlayerUUID}")
            val code = response.code
            val body = response.body.string()
            if (code == 404 || body.contains("PLAYER_DOES_NOT_EXIST")) {
                // No party found so player is not on Valorant
                timerSeconds = 10000
                notInGame()
                return
            } else if (code == 400 && body.contains("BAD_CLAIMS")) {
                // auth expired so tell user to restart app
                timerSeconds = 100000
                changePartyStatusText("Auth expired, please restart app")
                return
            } else if (code == 200) {
                timerSeconds = 1000
                val partyJSON = JSONObject(body)
                playerPartyID = partyJSON.getString("CurrentPartyID")
                getPartyDetails(playerPartyID!!)
            } else {
                Log.d("LIVE_STATS_PARTY_STATUS", "Error: $code")
                Log.d("LIVE_STATS_PARTY_STATUS", "Body: $body")
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
        val channelName = "channel_name"
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

        pingListView?.visibility = View.VISIBLE
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
                    playerReady
                )
            )
        }

        if (members.length() == 1) {
            // only one member so hide the listview
            view?.findViewById<TextView>(R.id.new_partyMembersText)?.text = "Only you in party"
            partyMemberListView?.visibility = View.INVISIBLE
            // clear the listview
            partyMemberListView?.adapter = null
            return
        } else {
            partyMemberListView?.visibility = View.VISIBLE
            view?.findViewById<TextView>(R.id.new_partyMembersText)?.text =
                members.length().toString() + " members in party"
            partyMemberListView?.adapter = PartyMemberAdapter(requireActivity(), partyMembers)
        }
    }

    private fun decodeNameFromSubject(subject: String): String {
        // check if authpreferences contains the subject
        if (authPreferences.contains(subject)) {
            // return the name
            return authPreferences.getString(subject, "")!!
        } else {
            // get the name from the subject
            val url = "https://pd.${shard}.a.pvp.net/name-service/v2/players"
            // body is an array of subjects
            val body = "[\"$subject\"]"

            val response = APIRequestValorant(url, body, true)
            val code = response.code
            val subjectBody = response.body.string()

            if (code != 200) return ""
            val playerName = JSONArray(subjectBody).getJSONObject(0).getString("GameName")
            val playerTag = JSONArray(subjectBody).getJSONObject(0).getString("TagLine")

            val displayName = "$playerName#$playerTag"

            // save the name to the shared preferences
            authPreferences.edit().putString(subject, displayName).apply()
            return displayName
        }
    }

    private fun getPartyDetails(partyID: String) {
        val url = "https://glz-${region}-1.${shard}.a.pvp.net/parties/v1/parties/${partyID}"
        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code
        if (code != 200) return
        partyState = JSONObject(body).getString("State")
        val previousState = JSONObject(body).getString("PreviousState")
        val currentModeSelected =
            JSONObject(body).getJSONObject("MatchmakingData").getString("QueueID")

        var isReady = true

        val members = JSONObject(body).getJSONArray("Members")
        handlePartyMembers(members)
        for (i in 0 until members.length()) {
            if (!members.getJSONObject(i).getBoolean("IsReady")) isReady = false
            break
        }

        // add a capital letter to the current mode selected
        var currentModeSelectedCapital =
            currentModeSelected[0].uppercaseChar() + currentModeSelected.substring(1)
        if (currentModeSelectedCapital == "Spikerush") currentModeSelectedCapital = "Spike Rush"
        if (currentModeSelectedCapital == "Swiftplay") currentModeSelectedCapital = "Swift Play"
        if (currentModeSelectedCapital == "Ggteam") currentModeSelectedCapital = "Escalation"
        if (currentModeSelectedCapital == "Onefa") currentModeSelectedCapital = "Replication"

        val spinner = view?.findViewById<Spinner>(R.id.new_partyGameModeSelect)
        val currentModeSelectedIndex = gameModes.indexOf(currentModeSelectedCapital)
        spinner?.setSelection(currentModeSelectedIndex)
        handlePartyState(partyState!!, previousState, isReady)
        Log.d("LIVE_STATS_PARTY_STATUS", "Party state: $body")

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
            joinMatchButton.text = "Cancel queue"
            changePartyStatusText("Matchmaking...")
            hideLayoutsMatch()
        } else if (state == "DEFAULT" && previousState == "LEAVING_MATCHMAKING" || previousState == "DEFAULT") {
            joinMatchButton.text = "Join queue"
            hideLayoutsMatch()
            changePartyStatusText("In Lobby")
        } else if (state == "DEFAULT" && previousState == "MATCHMADE_GAME_STARTING" && !isReady!!) {
            changePartyStatusText("Party is not ready")
            joinMatchButton.text = "Not ready"
            joinMatchButton.alpha = 0.5f
            joinMatchButton.isEnabled = false
            getGameInfoPlayer()
        } else if (state == "DEFAULT" && previousState == "MATCHMADE_GAME_STARTING" && isReady!!) {
            joinMatchButton.text = "Join queue"
            hideLayoutsMatch()
            changePartyStatusText("In Lobby")
        }
    }

    private fun notInGame() {
        changePartyStatusText("Not in game!")
        val findMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
        findMatchButton!!.alpha = 0.5f
        findMatchButton.isEnabled = false

        val PingsList = view?.findViewById<ListView>(R.id.new_partyPingsListView)
        PingsList?.visibility = View.GONE

        val new_partyMembersText = view?.findViewById<TextView>(R.id.new_partyMembersText)
        new_partyMembersText?.text = "Not in game!"

        playerPartyID = null
        partyState = null
    }

    private fun getGameInfoPlayer() {
        val PreGameLayout = view?.findViewById<RelativeLayout>(R.id.new_LayoutPartyPreGame)

        if (PlayerUUID == null) return
        val url = "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/players/${PlayerUUID}"
        val response = APIRequestValorant(url)
        val code = response.code
        val body = response.body.string()

        if (code == 200) {
            val preGameJSON = JSONObject(body)
            val matchID = preGameJSON.getString("MatchID")
            PreGameLayout?.visibility = View.VISIBLE
            playerPreGame(matchID)
        } else if (code == 404) {
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

    private fun hideLayoutsMatch() {
        val pregameLayout = view?.findViewById<RelativeLayout>(R.id.new_LayoutPartyPreGame)
        pregameLayout?.visibility = View.GONE
        val lockInButton = view?.findViewById<Button>(R.id.new_lockInButton)
        lockInButton!!.alpha = 1.0f
        lockInButton.isEnabled = true
        lockInButton.text = "Select Agents"
    }

    private fun playerPreGame(matchID: String) {
        val url = "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/matches/${matchID}"
        val response = APIRequestValorant(url)

        val code = response.code
        val body = response.body.string()
        if (code != 200) {
            notificationSent = false
            return
        }

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

        val currentModeSelected = preGameJSON.getString("QueueID")
        // add a capital letter to the current mode selected
        var currentModeSelectedCapital =
            currentModeSelected[0].uppercaseChar() + currentModeSelected.substring(1)
        if (currentModeSelectedCapital == "Spikerush") currentModeSelectedCapital = "Spike Rush"
        if (currentModeSelectedCapital == "Swiftplay") currentModeSelectedCapital = "Swift Play"
        if (currentModeSelectedCapital == "Ggteam") currentModeSelectedCapital = "Escalation"
        if (currentModeSelectedCapital == "Onefa") currentModeSelectedCapital = "Replication"
        val textViewMode = view?.findViewById<TextView>(R.id.new_partyPreGameTitle)
        textViewMode?.text = "Playing $currentModeSelectedCapital"

        sendNotification(
            "Match Found! $currentModeSelectedCapital",
            "Select your agent!",
            "match_found"
        )

        val mapName = preGameJSON.getString("MapID")
        val pregameBackground = view?.findViewById<ImageView>(R.id.new_partyPreGameMapImage)
        Picasso.get().load(MapsImagesID[mapName]).fit().centerCrop().into(pregameBackground)
    }

    private fun quitMatch(matchID: String) {
        // Show a dialog to confirm the user wants to quit the match
        val builder = AlertDialog.Builder(context, R.style.AlertDialogTheme)
        builder.setTitle("Quit match")
        builder.setMessage("Are you sure you want to quit the match? (This could result in a penalty)")
        builder.setPositiveButton("Yes") { dialog, which ->
            // get the match ID
            val url =
                "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/matches/${matchID}/quit"
            APIRequestValorant(url, "")
//            val code = response.code
//            if (code == 200) {
//                Toast.makeText(context, "Successfully quit the match", Toast.LENGTH_SHORT).show()
//            } else {
//                Toast.makeText(context, "Failed to quit the match", Toast.LENGTH_SHORT).show()
//            }
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
        val preGameresponse = APIRequestValorant(preGameUrl)
        val preGamecode = preGameresponse.code
        val preGamebody = preGameresponse.body.string()

        if (preGamecode != 200) return

        val preGameJSON = JSONObject(preGamebody)
        val matchID = preGameJSON.getString("MatchID")

        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/matches/${matchID}/select/${agentID}"
        val response = APIRequestValorant(url, "")

        val code = response.code

        if (code == 200) {
            val agentName = AgentNamesID[agentID]
            changePartyStatusText("Selected $agentName")
            val agentButton = view?.findViewById<Button>(R.id.new_lockInButton)
            agentButton?.text = "Lock in $agentName"
        }
    }

    private fun lockInCharacter(agentID: String) {
        // check if the pregame layout is visible
        val preGameLayout = view?.findViewById<RelativeLayout>(R.id.new_LayoutPartyPreGame)
        if (preGameLayout?.visibility == View.GONE) return

        if (PlayerUUID == null) return
        val preGameUrl =
            "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/players/${PlayerUUID}"
        val preGameresponse = APIRequestValorant(preGameUrl)
        val preGamecode = preGameresponse.code
        val preGamebody = preGameresponse.body.string()

        if (preGamecode != 200) return

        val preGameJSON = JSONObject(preGamebody)
        val matchID = preGameJSON.getString("MatchID")

        val url =
            "https://glz-${region}-1.${shard}.a.pvp.net/pregame/v1/matches/${matchID}/lock/${agentID}"
        val response = APIRequestValorant(url, "")

        val code = response.code

        if (code == 200) {
            val agentName = AgentNamesID[agentID]
            changePartyStatusText("Selected $agentName")
            val lockInButton = view?.findViewById<Button>(R.id.new_lockInButton)
            lockInButton?.isEnabled = false
            lockInButton?.alpha = 0.5f
            lockInButton?.text = "Locked in $agentName"
        }
    }

    private fun playerCoreGame(matchID: String) {

    }

    private fun getAgentImages(): ArrayList<String> {
        val agentImages = ArrayList<String>()

        // go thru hashmap
        for (agent in AgentNamesID.keys) {
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

        val response = APIRequestValorant(url, body)
        val code = response.code
        if (code == 200) {
            changePartyStatusText("Queue changed to $mode")
            // delay coroutine to allow time for party state to change
            GlobalScope.launch {
                delay(1000)
                withContext(Dispatchers.Main)
                {
                    changePartyStatusText("In Lobby")
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
                INITMode()
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

        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code

        if (code != 200) return arrayListOf()

        Log.d("LIVE_STATS_AVAILABLE_SPRAYS", body)
        val sprays = JSONObject(body).getJSONArray("Entitlements")
        for (i in 0 until sprays.length()) {
            val sprayObject = sprays.getJSONObject(i)
            val sprayID = sprayObject.getString("ItemID")
            availableSprays.add(sprayID)
        }
        return availableSprays
    }

    private fun getAvailablePlayerCards(): ArrayList<String> {
        val availablePlayerCards = arrayListOf<String>()
        val playerCardID = "3f296c07-64c3-494c-923b-fe692a4fa1bd\t"
        val url = "https://pd.${shard}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$playerCardID"

        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code

        if (code != 200) return arrayListOf()

        Log.d("LIVE_STATS_AVAILABLE_PLAYERCARDS", body)
        val playerCards = JSONObject(body).getJSONArray("Entitlements")
        for (i in 0 until playerCards.length()) {
            val playerCardObject = playerCards.getJSONObject(i)
            val playerCardID = playerCardObject.getString("ItemID")
            availablePlayerCards.add(playerCardID)
        }
        return availablePlayerCards
    }

    private fun getAvailableTitles(): ArrayList<String> {
        val availableTitles = arrayListOf<String>()
        val titleID = "de7caa6b-adf7-4588-bbd1-143831e786c6\t"
        val url = "https://pd.${shard}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$titleID"

        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code

        if (code != 200) return arrayListOf()

        Log.d("LIVE_STATS_AVAILABLE_TITLES", body)

        val titles = JSONObject(body).getJSONArray("Entitlements")
        for (i in 0 until titles.length()) {
            val titleObject = titles.getJSONObject(i)
            val titleID = titleObject.getString("ItemID")

            // convert titleID to title name
            val converted = assetsDB.retrieveName(titleID)
            availableTitles.add(converted)
        }
        // sort titles alphabetically
        availableTitles.sort()
        return availableTitles
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

    private fun APIRequestValorant(
        url: String,
        body: String? = null,
        put: Boolean? = false
    ): Response {
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
                return@runBlocking client.newCall(request).execute()
            }
        }
    }

    // override when application is not in focus
    override fun onPause() {
        super.onPause()
        Log.d("LIVE_STATS", "onPause")
        timerSeconds = 10000
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
