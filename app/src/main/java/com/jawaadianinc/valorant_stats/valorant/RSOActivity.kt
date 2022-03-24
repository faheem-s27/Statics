package com.jawaadianinc.valorant_stats.valorant

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Base64
import android.util.Log
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL


class RSOActivity : AppCompatActivity() {
    var code: String? = null
    var secret: String? = null
    var base64encode: String? = null
    var redirectURL = "https://statics-fd699.web.app/authorize.html"
    private lateinit var imagebackground: ImageView
    private val imagesURL = java.util.ArrayList<String>()
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rsoactivity)
        val data: Uri? = intent?.data
        val updateText: TextView = findViewById(R.id.infoText)
        code = data!!.getQueryParameter("code")
        updateText.text = "Processing data"

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

        progressBar.progress = 10

        val database = Firebase.database.getReference("VALORANT/SuperSecret")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                progressBar.progress = 25
                secret = dataSnapshot.value as String?
                val toBeEncoded = "statics:$secret"
                base64encode = Base64.encodeToString(toBeEncoded.toByteArray(), Base64.NO_WRAP)
                updateText.text = "Signing in"
                getToken()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun getToken() {
        val updateText: TextView = findViewById(R.id.infoText)
        try {
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
                .addHeader("Authorization", Credentials.basic("statics", secret!!))
                .post(formBody)
                .build()
            Log.d("RSO", "${Credentials.basic("statics", secret!!)}")

            doAsync {
                val call = client.newCall(request).execute()
                val json = JSONObject(call.body!!.string())
                Log.d("RSO", "Token JSON: $json")
                val accessToken = json.getString("access_token")
                progressBar.progress = 40
                uiThread {
                    getUserInfo(accessToken)
                }
            }
        } catch (e: Exception) {
            updateText.text = "Error: $e"
        }
    }

    private fun getUserInfo(token: String) {
        val updateText: TextView = findViewById(R.id.infoText)
        updateText.text = "Gathering data"
        progressBar.progress = 75
        val client = OkHttpClient()
        val urlBuilder: HttpUrl.Builder =
            "https://auth.riotgames.com/userinfo".toHttpUrlOrNull()!!.newBuilder()
        val url = urlBuilder.build().toString()
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $token")
            .build()

        doAsync {
            val call = client.newCall(request).execute()
            val json = JSONObject(call.body!!.string())
            Log.d("RSO", "User JSON: $json")
            val sub = json.getString("sub")
            uiThread {
                progressBar.progress = 90
                updateText.text = "Downloading matches"
            }
        }
    }

    private fun doTask(handler: Handler) {
        Picasso.get().load(imagesURL.random()).placeholder(imagebackground.drawable)
            .into(imagebackground)
        handler.postDelayed(runnable, 3000)
    }

}
