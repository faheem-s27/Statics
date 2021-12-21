package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class brawlFindAccount : AppCompatActivity() {
    var ENDPOINT = ""
    var name: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brawl_find_account)
        val brawlID: EditText = findViewById(R.id.brawlID)
        val search: Button = findViewById(R.id.brawlStats)
        val username: TextView = findViewById(R.id.name)
        val database = Firebase.database
        val playersRef = database.getReference("Brawlhalla/players")

        val images = ""

        brawlID.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable) {}
            override fun beforeTextChanged(
                s: CharSequence, start: Int,
                count: Int, after: Int
            ) {
            }

            override fun onTextChanged(
                s: CharSequence, start: Int,
                before: Int, count: Int
            ) {
                if (s.toString().isNotEmpty()) {
                    username.text = "Finding..."
                    ENDPOINT = "/player/${s}/stats?api_key=${Companion.APIcode}"
                    val requestURL = URL + ENDPOINT
                    var json: JSONObject? = null
                    async {
                        json = JSONObject(URL(requestURL).readText())
                        if (json != null) {
                            try {
                                name = json!!["name"] as String
                                uiThread {
                                    playersRef.child(s.toString()).child("playerName")
                                        .setValue(name)
                                    username.text = name
                                }
                            } catch (e: Exception) {
                                uiThread {
                                    username.text = "No account found"
                                }
                            }
                        } else {
                            uiThread {
                                username.text = "No account found"
                            }
                        }
                    }
                } else {
                    username.text = "Type a Brawl ID"
                }
            }
        })

        search.setOnClickListener {
            if (brawlID.text.toString().isNotEmpty()) {
                val intent = Intent(this, brawlStatsActivity::class.java)
                intent.putExtra("BrawlID", brawlID.text.toString())
                intent.putExtra("BrawlName", name)
                startActivity(intent)
            }
        }
    }

    companion object {
        const val APIcode = "AK4GDM78QDQH4NFR4AZI"
        const val URL = "https://api.brawlhalla.com"
    }
}