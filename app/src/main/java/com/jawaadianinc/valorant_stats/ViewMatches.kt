package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.json.JSONObject
import java.net.URL

class ViewMatches : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_matches)

        val userName = intent.extras!!.getString("RiotName")
        val totalMatches: TextView = findViewById(R.id.totalMatches)
        val matchList: ListView = findViewById(R.id.matchList)
        val downloadedText: TextView = findViewById(R.id.downloaded)
        val playerImage: ImageView = findViewById(R.id.backgroundPlayer)

        val database = MatchDatabases(this)
        totalMatches.gravity = Gravity.CENTER
        totalMatches.text =
            "$userName\n" + database.getTotalUserMatches(userName!!) + " matches saved"

        downloadedText.text = "Downloaded ${database.getTotalMatches()} total matches!"

        val arrayList = ArrayList<String>()
        val mAdapter = object :
            ArrayAdapter<String?>(
                this, android.R.layout.simple_expandable_list_item_1,
                arrayList as List<String?>
            ) {
            override fun getView(
                position: Int,
                convertView: View?,
                parent: ViewGroup
            ): View {
                val item = super.getView(position, convertView, parent) as TextView
                item.setTextColor(Color.parseColor("#FFFFFF"))
                item.setTypeface(item.typeface, Typeface.BOLD)
                item.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)

                return item
            }
        }

        matchList.adapter = mAdapter
        val listofMatches = database.getallMatches(userName)
        for (i in listofMatches) {
            mAdapter.add(i)
        }

        matchList.setOnItemClickListener { _, _, position, _ ->
            val wholeString = matchList.getItemAtPosition(position) as String
            val splitting = wholeString.split(":\n")
            val matchID = database.fetchMatchID(splitting[1].toInt())
            val splitName = userName.split("#")
            val RiotName = splitName[0]
            val ID = splitName[1]
            matchActivityStart(RiotName, ID, 0, matchID!!)
        }

        val split = userName.split("#")
        val Name = split[0]
        val ID = split[1]

        val PlayerURL = "https://api.henrikdev.xyz/valorant/v1/account/${Name}/$ID?force=true"

        doAsync {
            val text = URL(PlayerURL).readText()
            var data = JSONObject(text)
            data = data["data"] as JSONObject
            val cards = data["card"] as JSONObject
            val playerCard = cards["large"].toString()
            runOnUiThread {
                Picasso
                    .get()
                    .load(playerCard)
                    .fit()
                    .centerCrop()
                    .into(playerImage)

            }
        }


    }

    private fun matchActivityStart(Name: String, ID: String, matchNumber: Int, matchID: String) {
        val matchintent = Intent(this@ViewMatches, MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", matchNumber)
        matchintent.putExtra("MatchID", matchID)
        startActivity(matchintent)

    }
}