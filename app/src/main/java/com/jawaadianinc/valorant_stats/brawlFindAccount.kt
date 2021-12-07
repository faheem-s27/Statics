package com.jawaadianinc.valorant_stats

import android.app.ProgressDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.jetbrains.anko.custom.async
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class brawlFindAccount : AppCompatActivity() {
    var ENDPOINT = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_brawl_find_account)

        val brawlName: EditText = findViewById(R.id.brawlName)
        val brawlID: EditText = findViewById(R.id.brawlID)
        val save: Button = findViewById(R.id.saveName)
        val search: Button = findViewById(R.id.search)
        val username: TextView = findViewById(R.id.name)
        val mySpinner: Spinner = findViewById(R.id.listofNames)
        save.setOnClickListener {
            if (brawlName.text.isNotEmpty()) {
                if (brawlID.text.isNotEmpty()) {
                    Toast.makeText(this, "Saving!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        search.setOnClickListener {
            if (brawlID.text.isNullOrBlank()) {
                Toast.makeText(this, "ID is empty!", Toast.LENGTH_SHORT).show()
            } else {
                ENDPOINT = "/player/${brawlID.text}/stats?api_key=${Companion.APIcode}"
                val requestURL = URL + ENDPOINT
                val progressDialog = ProgressDialog(this)
                progressDialog.setTitle("Fetching Name")
                progressDialog.setMessage("Please wait a moment")
                progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
                progressDialog.setCancelable(false)
                //progressDialog.show()
                var json: JSONObject? = null

                async {
                    json = JSONObject(URL(requestURL).readText())
                    if (json != null) {
                        val name = json!!["name"] as String
                        uiThread {
                            username.text = "Your Brawl name: $name"
                            //progressDialog.dismiss()
                        }
                    }
                }
            }
        }
    }

    companion object {
        const val APIcode = "AK4GDM78QDQH4NFR4AZI"
        const val URL = "https://api.brawlhalla.com"
    }
}