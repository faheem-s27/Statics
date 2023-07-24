package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import com.jawaadianinc.valorant_stats.valo.databases.Match
import org.jetbrains.anko.find

data class MatchAnalyser(val sortedList: Array<Pair<String, Int>>, val type: String, val assetName: List<String>) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MatchAnalyser

        if (!sortedList.contentEquals(other.sortedList)) return false

        return true
    }

    override fun hashCode(): Int {
        return sortedList.contentHashCode()
    }
}

class MatchesProcessedStats : AppCompatActivity() {
    lateinit var agentsCount: Array<Pair<String, Int>>
    lateinit var mapsCount: Array<Pair<String, Int>>
    lateinit var assetsDB: AssetsDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches_processed_stats)
3
        agentsCount = (intent.getSerializableExtra("agents") as? Array<Pair<String, Int>>)!!
        mapsCount = (intent.getSerializableExtra("maps") as? Array<Pair<String, Int>>)!!
        assetsDB = AssetsDatabase(this)

        val listStats = mutableListOf<MatchAnalyser>()

        val agentsActualNames = mutableListOf<String>()
        val mapsActualName = mutableListOf<String>()
        for (agent in agentsCount)
        {
            agentsActualNames+=assetsDB.retrieveName(agent.first)
        }
        for (map in mapsCount)
        {
            mapsActualName+=assetsDB.retrieveName(map.first)
        }

        listStats.add(MatchAnalyser(agentsCount, "Agent", agentsActualNames))
        listStats.add(MatchAnalyser(mapsCount, "Map", mapsActualName))

        val adapter = MatchesAnalyserAdapter(listStats)
        val recyclerView = findViewById<RecyclerView>(R.id.recylcerview_matches_data)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

    }
}