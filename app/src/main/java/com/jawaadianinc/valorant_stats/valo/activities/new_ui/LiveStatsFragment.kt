package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class LiveStatsFragment : Fragment() {
    lateinit var playerName: String
    lateinit var region: String
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
        "ew0KCSJwbGF0Zm9ybVR5cGUiOiAiUEMiLA0KCSJwbGF0Zm9ybU9TIjogIldpbmRvd3MiLA0KCSJwbGF0Zm9ybU9TVmVyc2lvbiI6ICIxMC4wLjE5MDQyLjEuMjU2LjY0Yml0IiwNCgkicGxhdGZvcm1DaGlwc2V0IjogIlVua25vd24iDQp"
    lateinit var gameModes: Array<String>

    var playerPartyID: String? = null
    var partyState: String? = null

    private var partyExpanded = true
    private var loadoutExpanded = true
    private var SpraysIDImage = HashMap<String, String>()
    private var AgentNamesID = HashMap<String, String>()

    private var timerSeconds: Long = 500
    lateinit var assetsDB: AssetsDatabase

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

        INITVIEW = requireView().findViewById(R.id.InitView)
        LIVEVIEW = requireView().findViewById(R.id.LiveView)

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

            val spraysURL = "https://valorant-api.com/v1/sprays"
            val spraysJSON = JSONObject(URL(spraysURL).readText())
            val spraysData = spraysJSON.getJSONArray("data")
            for (i in 0 until spraysData.length()) {
                val spray = spraysData.getJSONObject(i)
                SpraysIDImage[spray.getString("uuid")] = spray.getString("fullTransparentIcon")
            }

            val agentsURL = "https://valorant-api.com/v1/agents"
            val agentsJSON = JSONObject(URL(agentsURL).readText())
            val agentsData = agentsJSON.getJSONArray("data")
            for (i in 0 until agentsData.length()) {
                val agent = agentsData.getJSONObject(i)
                AgentNamesID[agent.getString("uuid")] = agent.getString("displayName")
            }

        }

        val username = authPreferences.getString("username", null)
        val password = authPreferences.getString("password", null)

        if (username != null && password != null) {
            authenticateUser(username, password)
        } else {
            requireView().findViewById<Button>(R.id.continueInit2).setOnClickListener {
                loadUI("INIT")
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

            val builder = AlertDialog.Builder(requireActivity())
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
                    authenticateUser(username, password)
                }
                .setNegativeButton("Cancel", null)
            builder.show()
        }
    }

    private fun authenticateUser(username: String, password: String) {
        // show the Statics Dialog Progress Bar
        val dialog =
            ProgressDialogStatics().setProgressDialog(requireActivity(), "Authenticating...")
        //dialog.show()

        // create a new coroutine scope
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val mediaType = "application/json".toMediaTypeOrNull()
            val authBody = AuthCookiesBody()
            val authBodyJson = Gson().toJson(authBody).toRequestBody(mediaType)

            val clientPlatformBody = ClientPlatformBody()
            val clientPlatformBodyJson = Gson().toJson(clientPlatformBody).toRequestBody(mediaType)

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

            if (response.code != 200) {
                withContext(Dispatchers.Main)
                {
                    dialog.dismiss()
                    val body = response.body.string()
                    val msg = "Response code: $code\nBody: $body"
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Response from Auth Cookies")
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                    dialog.show()
                }
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

            val authBodyUserNamePassword = AuthRequestBody(username, password)
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
                    dialog.dismiss()
                    val msg = "Response was successful but username or password is incorrect"
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Response from Statics")
                        .setMessage(msg)
                        .setPositiveButton("OK") { _, _ ->
                            // clear the username and password from authPrefs
                            authPreferences.edit().remove("username").apply()
                            authPreferences.edit().remove("password").apply()

                            // dismiss the dialog
                            dialog.dismiss()

                            // refresh the activity
                            requireActivity().recreate()
                        }
                    dialog.show()
                }
                return@launch
            }

            withContext(Dispatchers.Main)
            {
                dialog.dismiss()
                // store authCookieHeader in shared preferences
                //authPreferences.edit().putString("cookieAuth", authCookieHeader).apply()
                storeEverything(username, password, authResponseBody, authCookieHeader)
            }
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
                val builder = AlertDialog.Builder(context)
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

    private fun send2FAcode(code: String, cookieHeader: String) {
        val multifactorRequest = Request.Builder()
            .url("https://auth.riotgames.com/api/v1/authorization")
            .addHeader("Content-Type", "application/json")
            .addHeader("Accept", "application/json")
            .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
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
            Log.d("LIVE_STATS_2FA", "Response code: ${responseMFA.code} Body: $body")
            withContext(Dispatchers.Main)
            {
                if (responseMFA.code != 200) {
                    Log.d("LIVE_STATS_2FA", "Response code not 200")
                    val msg = "Response code: $code\nBody: $body"
                    val dialog = AlertDialog.Builder(requireContext())
                        .setTitle("Response from Statics")
                        .setMessage(msg)
                        .setPositiveButton("OK", null)
                    dialog.show()
                } else {
                    Log.d("LIVE_STATS_2FA", "Response code 200")
                    val json = JSONObject(body)
                    Log.d("LIVE_STATS_2FA", "JSON: $json")
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

                    Log.d("LIVE_STATS_2FA", "No error in response")

                    val mfaCookies = responseMFA.headers("Set-Cookie")
                    val mfaCookieHeader = TextUtils.join("; ", mfaCookies)
                    // save the cookie
                    authPreferences.edit().putString("cookieMFA", mfaCookieHeader)
                        .apply()

                    val uri = json.getJSONObject("response").getJSONObject("parameters")
                        .getString("uri")
                    val access =
                        uri.substringAfter("access_token=").substringBefore("&")
                    getEntitlement(access)
                }
            }
        }
    }

    private fun getEntitlement(access: String) {
        GlobalScope.launch {
            val request = Request.Builder()
                .url("https://entitlements.auth.riotgames.com/api/token/v1")
                .addHeader("Content-Type", "application/json")
                .addHeader("X-Riot-ClientPlatform", clientPlatformToken)
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

        liveSetup()
        timerLiveAPI()
        getPlayerLoadOuts()
        //getContracts()
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

        val url = "https://pd.${region}.a.pvp.net/store/v1/wallet/$PlayerUUID"
        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code

        Log.d("LIVE_STATS_CONTRACTS", "Response code: $code Body: $body")

        if (code == 200) {
            //Log.d("LIVE_STATS_CONTRACTS", "Response: $body")
        }
    }

    private fun getPlayerLoadOuts() {
        if (PlayerUUID == null) {
            return
        }
        val url =
            "https://pd.${region}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code

        if (code == 200) {
            // show dialog
            val json = JSONObject(body)
            loadSprays(json.getJSONArray("Sprays"))
            loadTitle(json.getJSONObject("Identity").getString("PlayerTitleID"))
        }
    }

    private fun loadTitle(TitleID: String) {
        val TitleView = view?.findViewById<TextView>(R.id.CurrentLoadoutTitle)
        val titleName = assetsDB.retrieveName(TitleID)
        TitleView?.text = titleName

        val CurrentLoadoutTitleEdit = view?.findViewById<ImageView>(R.id.CurrentLoadoutTitleEdit)
        CurrentLoadoutTitleEdit?.setOnClickListener {
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
            dialog.show()
        }
    }

    private fun updateTitle(titleID: String) {
        val url =
            "https://pd.${region}.a.pvp.net/personalization/v2/players/${PlayerUUID}/playerloadout"
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

    private fun loadSprays(Sprays: JSONArray) {
        val PreRoundSprayView = view?.findViewById<ImageView>(R.id.CurrentLoadoutSprayPreRound)
        val MidRoundSprayView = view?.findViewById<ImageView>(R.id.CurrentLoadoutSprayMidRound)
        val PostRoundSprayView = view?.findViewById<ImageView>(R.id.CurrentLoadoutSprayPostRound)

        Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(1).getString("SprayID")]).fit()
            .centerInside().into(PreRoundSprayView)
        Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(2).getString("SprayID")]).fit()
            .centerInside().into(MidRoundSprayView)
        Picasso.get().load(SpraysIDImage[Sprays.getJSONObject(0).getString("SprayID")]).fit()
            .centerInside().into(PostRoundSprayView)


        PreRoundSprayView!!.setOnClickListener {
        }
    }

    private fun liveSetup() {
        val spinner = view?.findViewById<Spinner>(R.id.new_partyGameModeSelect)
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, gameModes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner?.adapter = adapter

        val name = view?.findViewById<TextView>(R.id.new_partyPlayerName)
        name?.text = playerName.split("#")[0]
        val tag = view?.findViewById<TextView>(R.id.new_partyPlayerTag)
        tag?.text = "#" + playerName.split("#")[1]

        val image = view?.findViewById<ImageView>(R.id.new_playerAvatar)
        Picasso.get().load(StaticsMainActivity.playerCardSmall).into(image)

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
    }

    private fun joinMatchmaking() {
        val url =
            "https://glz-${region}-1.${region}.a.pvp.net/parties/v1/parties/${playerPartyID}/matchmaking/join"
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
            "https://glz-${region}-1.${region}.a.pvp.net/parties/v1/parties/${playerPartyID}/matchmaking/leave"
        val response = APIRequestValorant(url, "")
        val code = response.code
        if (code == 200) {
            val partyStatus = view?.findViewById<TextView>(R.id.new_playerPartyStatus)
            partyStatus?.text = "In Lobby"
            val joinMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
            joinMatchButton?.text = "Join queue"
        }
    }

    private fun getPartyStatus() {
        try {
            val response =
                APIRequestValorant("https://glz-${region}-1.${region}.a.pvp.net/parties/v1/players/${PlayerUUID}")
            val code = response.code
            val body = response.body.string()
            if (code == 404 || body.contains("PLAYER_DOES_NOT_EXIST")) {
                // No party found so player is not on Valorant
                timerSeconds = 10000
                notInGame()
                return
            } else if (code == 400 && body.contains("BAD_CLAIMS")) {
                // auth expired so tell user to restart app
                changePartyStatusText("Auth expired, please restart app")
                return
            } else if (code == 200) {
                timerSeconds = 500
                changePartyStatusText("Online!")
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

    private fun getPartyDetails(partyID: String) {
        val url = "https://glz-${region}-1.${region}.a.pvp.net/parties/v1/parties/${partyID}"
        val response = APIRequestValorant(url)
        val body = response.body.string()
        val code = response.code
        if (code != 200) return
        partyState = JSONObject(body).getString("State")
        val previousState = JSONObject(body).getString("PreviousState")
        val currentModeSelected =
            JSONObject(body).getJSONObject("MatchmakingData").getString("QueueID")

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
        handlePartyState(partyState!!, previousState)
        Log.d("LIVE_STATS_PARTY_STATUS", "Party state: $body")
    }

    private fun handlePartyState(state: String, previousState: String? = null) {
        val joinMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
        joinMatchButton!!.alpha = 1.0f
        joinMatchButton.isEnabled = true
        if (state == "MATCHMAKING") {
            joinMatchButton.text = "Cancel queue"
            changePartyStatusText("Matchmaking...")
        } else if (state == "DEFAULT" && previousState == "LEAVING_MATCHMAKING") {
            joinMatchButton.text = "Join queue"
            changePartyStatusText("In Lobby")
        } else if (state == "DEFAULT" && previousState == "MATCHMADE_GAME_STARTING") {
            changePartyStatusText("In a game!")
            joinMatchButton.text = "Already in a game"
            joinMatchButton.alpha = 0.5f
            joinMatchButton.isEnabled = false
        }
    }

    private fun notInGame() {
        changePartyStatusText("Not in game!")
        val findMatchButton = view?.findViewById<Button>(R.id.new_findMatchButton)
        findMatchButton!!.alpha = 0.5f
        findMatchButton.isEnabled = false

        playerPartyID = null
        partyState = null
    }

    private fun changeQueue(mode: String) {
        if (playerPartyID == null) return
        var queueID = mode.lowercase().replace(" ", "")
        if (queueID == "escalation") queueID = "ggteam"
        if (queueID == "replication") queueID = "onefa"

        val url =
            "https://glz-${region}-1.${region}.a.pvp.net/parties/v1/parties/${playerPartyID}/queue"
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
        fadeIn.duration = 250
        fadeIn.repeatCount = 2
        fadeIn.repeatMode = Animation.REVERSE
        view.startAnimation(fadeIn)
    }

    private fun getAvailableTitles(): ArrayList<String> {
        val availableTitles = arrayListOf<String>()
        val titleID = "de7caa6b-adf7-4588-bbd1-143831e786c6\t"
        val url = "https://pd.${region}.a.pvp.net/store/v1/entitlements/${PlayerUUID}/$titleID"

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
        return availableTitles
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
}
