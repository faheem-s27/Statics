package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase

class MatchesProcessedStats : AppCompatActivity() {
    lateinit var agentsCount: Array<Pair<String, Int>>
    lateinit var assetsDB: AssetsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches_processed_stats)

        agentsCount = (intent.getSerializableExtra("agents") as? Array<Pair<String, Int>>)!!
        assetsDB = AssetsDatabase(this)

        val agentName = findViewById<TextView>(R.id.agentName)
        val actualName = assetsDB.retrieveName(agentsCount[0].first)
        agentName.text = "$actualName was played ${agentsCount[1].second} times"
    }
}