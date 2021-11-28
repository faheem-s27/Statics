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
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ViewMatches : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_view_matches)

        val userName = intent.extras!!.getString("RiotName")
        val totalMatches: TextView = findViewById(R.id.totalMatches)
        val matchList: ListView = findViewById(R.id.matchList)

        val database = MatchDatabases(this)
        totalMatches.gravity = Gravity.CENTER
        totalMatches.text =
            "$userName\n" + database.getTotalUserMatches(userName!!) + " matches saved"

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
            val splitting = wholeString.split(":")
            val matchID = splitting[1]

            val splitName = userName.split("#")
            val RiotName = splitName[0]
            val ID = splitName[1]
            matchActivityStart(RiotName, ID, 0, matchID)

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