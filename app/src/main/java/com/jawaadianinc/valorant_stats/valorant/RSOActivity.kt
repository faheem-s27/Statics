package com.jawaadianinc.valorant_stats.valorant

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R
import okhttp3.*
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import java.io.IOException


class RSOActivity : AppCompatActivity() {
    var code: String? = null
    var secret: String? = null
    var base64encode: String? = null
    var redirectURL = "https://statics-fd699.web.app/authorize.html"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rsoactivity)
        val data: Uri? = intent?.data
        val updateText: TextView = findViewById(R.id.infoText)
        code = data!!.getQueryParameter("code")
        updateText.text = "Processing data"

        val database = Firebase.database.getReference("VALORANT/SuperSecret")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                secret = dataSnapshot.value as String?
                val toBeEncoded = "statics:$secret"
                base64encode = Base64.encodeToString(toBeEncoded.toByteArray(), Base64.NO_WRAP)
                updateText.text = "Verifiying..."
                authenticate()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun authenticate() {
        val client = OkHttpClient()

        val requestBody: RequestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("code", code!!)
            .addFormDataPart("redirect_uri", redirectURL)
            .addFormDataPart("grant_type", "authorization_code")
            .build()

        val request = Request.Builder()
            .url("https://auth.riotgames.com/token")
            .addHeader("Authorization", "Basic $base64encode")
            .post(requestBody)
            .build()

        val updateText: TextView = findViewById(R.id.infoText)
        doAsync {
            try {
                client.newCall(request).enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        uiThread {
                            updateText.text = response.body?.string()
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        uiThread {
                            updateText.text = "Error: $e"
                        }
                    }
                })
            } catch (e: Exception) {
                uiThread {
                    updateText.text = "Error: $e"
                }
            }
        }
    }
}
