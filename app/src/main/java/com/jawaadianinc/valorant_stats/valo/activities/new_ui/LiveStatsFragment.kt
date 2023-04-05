package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
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
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import io.ktor.client.*
import kotlinx.coroutines.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class LiveStatsFragment : Fragment() {
    lateinit var playerName: String
    lateinit var region: String
    lateinit var authPreferences: SharedPreferences
    var accessToken: String? = null
    var entitlementToken: String? = null
    var PlayerUUID: String? = null
    lateinit var INITVIEW: View
    lateinit var SETUPVIEW: View
    lateinit var LIVEVIEW: View
    lateinit var CurrentScreen: String
    lateinit var SETUPProgressBar: ProgressBar

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

        accessToken = authPreferences.getString("accessTokenV2", null)
        entitlementToken = authPreferences.getString("entitlementTokenV2", null)

        INITVIEW = requireView().findViewById(R.id.InitView)
        SETUPVIEW = requireView().findViewById(R.id.SetUpView)
        LIVEVIEW = requireView().findViewById(R.id.LiveView)

        SETUPProgressBar = requireView().findViewById(R.id.progressBar7)

        loadUI("INIT")
        val continueButton = requireView().findViewById<Button>(R.id.continueInit)
        continueButton.setOnClickListener {
            loadUI("SETUP")
        }
        val usernamepassword: Button = requireView().findViewById(R.id.continueInit2)
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
        dialog.show()

        // create a new coroutine scope
        val scope = CoroutineScope(Dispatchers.IO)
        scope.launch {
            val client = OkHttpClient.Builder()
                .build()

            val authBody = AuthCookiesBody()
            val mediaType = "application/json".toMediaTypeOrNull()
            val hiBody = Gson().toJson(authBody).toRequestBody(mediaType)

            // Log each body
            Log.d("LIVE_STATS", "Auth Body: " + Gson().toJson(authBody))

            // create a new request
            val request = Request.Builder()
                .url("https://auth.riotgames.com/api/v1/authorization")
                .addHeader("Content-Type", "application/json")
                .post(hiBody)
                .build()

            // execute the request
            val response = client.newCall(request).execute()
            val cookies = response.headers("Set-Cookie")
            val cookie = cookies[0].split(";")[0]
            // get the response body
            val body = response.body.string()
            // get the response code
            val code = response.code

            withContext(Dispatchers.Main)
            {
                dialog.dismiss()
                val msg = "Response code: $code\nBody: $body"
                val dialog = AlertDialog.Builder(requireContext())
                    .setTitle("Response")
                    .setMessage(msg)
                    .setPositiveButton("OK", null)
                dialog.show()
            }
        }
    }

    private fun SETUPView() {
        val discordURL = "https://discord.gg/jwfJUQMPP7"
        requireView().findViewById<Button>(R.id.button).setOnClickListener {
            val dialog = AlertDialog.Builder(requireContext())
            dialog.setTitle("Join Discord")
            dialog.setMessage("Join the discord server and look for the channel called 'live_access'\nThere you will find the file you need to get live stats!")
            dialog.setPositiveButton("Join") { _, _ ->
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(discordURL))
                startActivity(browserIntent)
            }
            dialog.setNegativeButton("Cancel") { _, _ -> }
            dialog.show()
        }

        if (accessToken == null || entitlementToken == null) {
            getTokens()
        } else {
            testTokens(accessToken!!)
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun LIVEView() {
    }

    // function to check CurrentScreen and show the correct view
    private fun loadUI(mode: String) {
        CurrentScreen = mode
        when (CurrentScreen) {
            "INIT" -> {
                INITVIEW.visibility = View.VISIBLE
                SETUPVIEW.visibility = View.GONE
                LIVEVIEW.visibility = View.GONE
            }
            "SETUP" -> {
                INITVIEW.visibility = View.GONE
                SETUPVIEW.visibility = View.VISIBLE
                LIVEVIEW.visibility = View.GONE
                SETUPView()
            }
            "LIVE" -> {
                INITVIEW.visibility = View.GONE
                SETUPVIEW.visibility = View.GONE
                LIVEVIEW.visibility = View.VISIBLE
                LIVEView()
            }
            "ERROR" -> {
                INITVIEW.visibility = View.GONE
                SETUPVIEW.visibility = View.GONE
                LIVEVIEW.visibility = View.GONE
            }
        }
    }

    private fun testTokens(access: String) {
        CurrentScreen = "SETUP"
        val client = OkHttpClient()
        val url = "https://auth.riotgames.com/userinfo".toHttpUrlOrNull()
        // put access token in header as Bearer
        val request = Request.Builder()
            .addHeader("Authorization", "Bearer $access")
            .url(url!!)
            .build()

        GlobalScope.launch {
            try {
                val response = client.newCall(request).execute()
                val body = response.body.string()
                val JSON = JSONObject(body)
                val name = JSON.getString("username")
                PlayerUUID = JSON.getString("sub")
                withContext(Dispatchers.Main)
                {
                    val textView = requireView().findViewById<TextView>(R.id.textView38)
                    textView.text = "Logged in as\n$name"
                    textView.clearAnimation()
                    val continueButton = requireView().findViewById<Button>(R.id.button3)
                    continueButton.isClickable = true
                    continueButton.alpha = 1.0f

                    Toast.makeText(requireActivity(), "LIVE CLIENT ACTIVATED!", Toast.LENGTH_SHORT)
                        .show()

                    // hide progress bar
                    SETUPProgressBar.visibility = View.INVISIBLE
                    // show button
                    val IPButtonCheck = requireView().findViewById<Button>(R.id.button2)
                    IPButtonCheck.visibility = View.VISIBLE

                    continueButton.setOnClickListener {
                        loadUI("LIVE")
                    }
                }
            } catch (e: Exception) {
                val textView = requireView().findViewById<TextView>(R.id.textView38)
                textView.text = "Waiting for IP..."
                val continueButton = requireView().findViewById<Button>(R.id.button3)
                continueButton.isClickable = false
                continueButton.alpha = 0.5f
                withContext(Dispatchers.Main) {
                    val dialog = AlertDialog.Builder(requireActivity())
                    dialog.setTitle("Error")
                    dialog.setMessage("Error authorizing tokens.\n\nPlease enter IP again.\n\nError:\n${e.message}")
                    dialog.setPositiveButton("Enter") { _, _ ->
                        getTokens()
                    }
                    dialog.setNegativeButton("Cancel") { _, _ -> }
                    dialog.show()

                    // hide progress bar
                    SETUPProgressBar.visibility = View.INVISIBLE
                    // show button
                    val IPButtonCheck = requireView().findViewById<Button>(R.id.button2)
                    IPButtonCheck.visibility = View.VISIBLE
                }
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getTokens() {
        CurrentScreen = "SETUP"
        fadeRepeat(requireView().findViewById(R.id.textView38))

        val continueButton = requireView().findViewById<Button>(R.id.button3)
        continueButton.isClickable = false
        continueButton.alpha = 0.5f

        val IPInput = requireView().findViewById<EditText>(R.id.IPInput)
        // clear IPInput
        IPInput.setText("")


        val IPButtonCheck = requireView().findViewById<Button>(R.id.button2)
        IPButtonCheck.setOnClickListener {
            // hide keyboard
            val imm =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(requireView().windowToken, 0)

            // hide button
            IPButtonCheck.visibility = View.INVISIBLE
            // show progress bar
            SETUPProgressBar.visibility = View.VISIBLE

            // get value of IPInput
            val IPInput = requireView().findViewById<EditText>(R.id.IPInput)
            val IP = IPInput.text.toString()

            // check if IP is valid
            if (IP == "") return@setOnClickListener

            GlobalScope.launch {
                val client = OkHttpClient()
                val url = "http://$IP:5000/".toHttpUrlOrNull()
                val request = Request.Builder()
                    .url(url!!)
                    .build()
                try {
                    val response = client.newCall(request).execute()
                    val body = response.body.string()
                    if (response.code == 200) {
                        val JSON = JSONObject(body)
                        accessToken = JSON.getString("accessToken")
                        entitlementToken = JSON.getString("entitlementToken")

                        val editor = authPreferences.edit()
                        editor.putString("accessTokenV2", accessToken)
                        editor.putString("entitlementTokenV2", entitlementToken)
                        editor.apply()

                        withContext(Dispatchers.Main)
                        {
                            testTokens(accessToken!!)
                        }

                    } else {
                        withContext(Dispatchers.Main)
                        {
                            val dialog = AlertDialog.Builder(requireActivity())
                            dialog.setTitle("Error")
                            dialog.setMessage("Couldn't connect to IP. Please check your IP address and try again.")
                            dialog.setPositiveButton("OK") { _, _ -> }
                            dialog.show()

                            // show button
                            IPButtonCheck.visibility = View.VISIBLE
                            // hide progress bar
                            SETUPProgressBar.visibility = View.INVISIBLE
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main)
                    {
                        val dialog = AlertDialog.Builder(requireActivity())
                        dialog.setTitle("Error")
                        dialog.setMessage("Error getting tokens.\n\nError message:\n${e.message}")
                        dialog.setPositiveButton("OK") { _, _ -> }
                        dialog.show()

                        // show button
                        IPButtonCheck.visibility = View.VISIBLE
                        // hide progress bar
                        SETUPProgressBar.visibility = View.INVISIBLE
                    }
                    return@launch
                }


            }
        }
    }

    private fun fadeRepeat(view: View) {
        // repeat forever fade in and out
        val fadeIn = AlphaAnimation(0.0f, 1.0f)
        fadeIn.interpolator = AccelerateInterpolator()
        fadeIn.duration = 1000
        fadeIn.repeatCount = Animation.INFINITE
        fadeIn.repeatMode = Animation.REVERSE
        view.startAnimation(fadeIn)
    }


}
