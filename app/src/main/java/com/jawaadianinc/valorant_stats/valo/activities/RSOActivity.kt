package com.jawaadianinc.valorant_stats.valo.activities

import android.appwidget.AppWidgetManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.jawaadianinc.valorant_stats.BuildConfig
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.main.LoadingActivity
import com.jawaadianinc.valorant_stats.valo.databases.PlayerDatabase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.Credentials
import okhttp3.FormBody
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class RSOActivity : AppCompatActivity() {
    private var code: String? = null
    private var secret: String? = null
    private var base64encode: String? = null
    private var redirectURL = "https://statics-fd699.web.app/authorize.html"
    private lateinit var imagebackground: ImageView
    private val imagesURL = java.util.ArrayList<String>()
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rsoactivity)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val data: Uri? = intent?.data
        val updateText: TextView = findViewById(R.id.infoText)
        code = data!!.getQueryParameter("code")
        updateText.text = getString(R.string.s10)

        val confirmButton: Button = findViewById(R.id.confirmUserButton)
        confirmButton.alpha = 0f
        confirmButton.translationY = 50f

        imagesURL.add("https://media.valorant-api.com/playercards/3432dc3d-47da-4675-67ae-53adb1fdad5e/largeart.png")
        doAsync {
            val getValoImagesURL =
                JSONObject(URL("https://valorant-api.com/v1/playercards").readText())
            val images = getValoImagesURL["data"] as JSONArray
            for (i in 0 until images.length()) {
                val imageURL = images[i] as JSONObject
                imagesURL.add(imageURL["largeArt"].toString())
            }
        }

        progressBar = findViewById(R.id.progressBar3)
        imagebackground = findViewById(R.id.imageView5)
        Picasso.get().load(imagesURL.random()).into(imagebackground)
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            doTask(handler)
        }
        handler.postDelayed(runnable, 500)

        progressBar.progress = 25
        secret = BuildConfig.RIOT_SECRET
        val toBeEncoded = "statics:$secret"
        base64encode = Base64.encodeToString(toBeEncoded.toByteArray(), Base64.NO_WRAP)
        updateText.text = getString(R.string.s11)
        getToken()
    }

    private fun getToken() {
        val updateText: TextView = findViewById(R.id.infoText)
        try {
            val sharedPreferences = this.getSharedPreferences("auth", Context.MODE_PRIVATE)
            val bearerToken = Credentials.basic("statics", secret!!)

            val client = OkHttpClient()
            val urlBuilder: HttpUrl.Builder =
                "https://auth.riotgames.com/token".toHttpUrlOrNull()!!.newBuilder()
            val url = urlBuilder.build().toString()

            val formBody: RequestBody = FormBody.Builder()
                .add("code", code!!)
                .add("redirect_uri", redirectURL)
                .add("grant_type", "authorization_code")
                .build()

            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", bearerToken)
                .post(formBody)
                .build()

            doAsync {
                val call = client.newCall(request).execute()
                val json = JSONObject(call.body.string())
                val accessToken = json.getString("access_token")
                val refreshToken = json.getString("refresh_token")
                val idToken = json.getString("id_token")

                val editor = sharedPreferences.edit()
                editor.putString("bearerToken", bearerToken)
                editor.putString("accessToken", accessToken)
                editor.putString("refreshToken", refreshToken)
                editor.putString("idToken", idToken)
                editor.apply()

                progressBar.progress = 40
                uiThread {
                    updateText.text = getString(R.string.s12)
                    getUserInfo(accessToken)
                }
            }
        } catch (e: Exception) {
            updateText.text = "Error: $e"
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun getUserInfo(token: String) {
        try {
            val updateText: TextView = findViewById(R.id.infoText)
            progressBar.progress = 75
            val client = OkHttpClient()
            val urlBuilder: HttpUrl.Builder =
                "https://europe.api.riotgames.com/riot/account/v1/accounts/me".toHttpUrlOrNull()!!
                    .newBuilder()
            val url = urlBuilder.build().toString()
            val request = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .build()

            secret = BuildConfig.RIOT_API_KEY
            GlobalScope.launch {
                val call = client.newCall(request).execute()
                val code = call.code
                val json = JSONObject(call.body.string())
                // copy the json to the clipboard
                val clipboard: ClipboardManager =
                    getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                    "json",
                    "$json\n\nURL: $url\n\nToken: $token\n\nSecret: $secret"
                )
                clipboard.setPrimaryClip(clip)

                // check if status is 200
                if (code != 200) {
                    withContext(Dispatchers.Main) {
                        updateText.text = "Error: $code"
                    }
                    return@launch
                }

                val puuid = json.getString("puuid")
                val regionURL =
                    "https://europe.api.riotgames.com/riot/account/v1/active-shards/by-game/val/by-puuid/$puuid?api_key=$secret"
                val regionRequest = Request.Builder()
                    .url(regionURL)
                    .build()

                val regionCall = client.newCall(regionRequest).execute()
                val regionCode = regionCall.code
                val regionJson = JSONObject(regionCall.body.string())
                // copy the json to the clipboard
                val regionClip = ClipData.newPlainText("json", "$regionJson\n\nURL: $regionURL")
                clipboard.setPrimaryClip(regionClip)

                if (regionCode != 200) {
                    withContext(Dispatchers.Main) {
                        updateText.text = "Error: $regionCode"
                    }
                    return@launch
                }

                val region = regionJson.getString("activeShard")
                val gameName = json.getString("gameName")
                val gameTag = json.getString("tagLine")
                withContext(Dispatchers.Main)
                {
                    progressBar.progress = 90
                    updateText.text = getString(R.string.s9)
                    confirmUser(puuid, gameName, gameTag, region)
                }
            }
        } catch (e: Exception) {
            val updateText: TextView = findViewById(R.id.infoText)
            updateText.text = "Error: $e"
        }
    }

    private fun confirmUser(
        puuid: String,
        gameName: String,
        gameTag: String,
        region: String
    ) {
        val updateText: TextView = findViewById(R.id.infoText)
        val confirmButton: Button = findViewById(R.id.confirmUserButton)
        "$gameName#$gameTag".also { confirmButton.text = it }
        confirmButton.animate().alpha(1f).translationYBy(-50f).duration = 500

        confirmButton.setOnClickListener {
            //save name to database
            val playerdb = PlayerDatabase(this)
            if (playerdb.addPlayer(gameName, gameTag, puuid, region)) {
                progressBar.progress = 100
                "${this.getString(R.string.s13)}$gameName!".also { updateText.text = it }
                //take user to main valorant screen
                Toast.makeText(
                    this,
                    "${getString(R.string.s14)} $gameName!",
                    Toast.LENGTH_LONG
                ).show()

                val widgetIntent = Intent(this, LastMatchWidget::class.java)
                widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
                    ComponentName(applicationContext, LastMatchWidget::class.java)
                )
                widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
                sendBroadcast(widgetIntent)

                val intent = Intent(this, LoadingActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                finish()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.s15),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun doTask(handler: Handler) {
        Picasso.get().load(imagesURL.random()).placeholder(imagebackground.drawable)
            .into(imagebackground)
        handler.postDelayed(runnable, 3000)
    }
}
