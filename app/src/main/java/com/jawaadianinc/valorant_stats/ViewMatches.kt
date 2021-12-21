package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
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
        val mapSpinner: Spinner = findViewById(R.id.mapChooser)
        val modeSpinner: Spinner = findViewById(R.id.modeChooser)

        val mapList = java.util.ArrayList<String>()
        mapList.add("All maps")
        mapList.add("Ascent")
        mapList.add("Bind")
        mapList.add("Breeze")
        mapList.add("Fracture")
        mapList.add("Haven")
        mapList.add("Icebox")
        mapList.add("Split")

        val modeList = java.util.ArrayList<String>()
        modeList.add("All modes")
        modeList.add("Competitive")
        modeList.add("Unrated")
        modeList.add("Spike Rush")
        modeList.add("Deathmatch")
        modeList.add("Escalation")
        modeList.add("Replication")
        modeList.add("Custom Game")

        val mapAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, mapList)
        mapAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice)
        mapSpinner.adapter = mapAdapter

        val modeAdapter: ArrayAdapter<String> =
            ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, modeList)
        modeAdapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice)
        modeSpinner.adapter = modeAdapter

        val database = MatchDatabases(this)
        val fireBase = Firebase.database
        val splitName = userName!!.split("#")
        val playersRef = fireBase.getReference("VALORANT/players/" + splitName[0] + "/Matches")
        playersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (child in dataSnapshot.children) {
                    // Do magic here
                    database.addMatches(
                        child.key.toString(),
                        userName,
                        child.child("Map").value.toString(),
                        child.child("Mode").value.toString()
                    )
                }
                totalMatches.text =
                    "$userName\n" + database.getTotalUserMatches(userName!!) + " matches saved"
                downloadedText.text = "Downloaded ${database.getTotalMatches()} total matches!"
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })

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
            val splitting = wholeString.split(".")
            val matchID = database.fetchMatchID(splitting[0].toInt())
            val splitName = userName.split("#")
            val RiotName = splitName[0]
            val ID = splitName[1]
            matchActivityStart(RiotName, ID, matchID!!)
        }

        mapSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val text = mapSpinner.getItemAtPosition(position).toString()
                    val modeText = modeSpinner.selectedItem.toString()
                    if (text == "All maps") {
                        if (modeText == "All modes") {
                            mAdapter.clear()
                            val listofMatches = database.getallMatches(userName)
                            for (i in listofMatches) {
                                mAdapter.add(i)
                            }
                            mAdapter.notifyDataSetChanged()
                            return
                        }
                    }
                    mAdapter.clear()
                    val mapMatches = database.filterMap(userName, text)
                    for (i in mapMatches) {
                        mAdapter.add(i)
                    }
                    mAdapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }

        modeSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>,
                    view: View,
                    position: Int,
                    id: Long
                ) {
                    val text = modeSpinner.getItemAtPosition(position).toString()
                    val mapText = mapSpinner.selectedItem.toString()
                    if (text == "All modes") {
                        if (mapText == "All maps") {
                            mAdapter.clear()
                            val listofMatches = database.getallMatches(userName)
                            for (i in listofMatches) {
                                mAdapter.add(i)
                            }
                            mAdapter.notifyDataSetChanged()
                            return
                        }
                    }
                    mAdapter.clear()
                    val mapMatches = database.filterMode(userName, text)
                    for (i in mapMatches) {
                        mAdapter.add(i)
                    }
                    mAdapter.notifyDataSetChanged()
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    TODO("Not yet implemented")
                }
            }
        //END

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

    private fun matchActivityStart(Name: String, ID: String, matchID: String) {
        val matchintent = Intent(this@ViewMatches, MatchHistoryActivity::class.java)
        matchintent.putExtra("RiotName", Name)
        matchintent.putExtra("RiotID", ID)
        matchintent.putExtra("MatchNumber", 0)
        matchintent.putExtra("MatchID", matchID)
        startActivity(matchintent)

    }
}