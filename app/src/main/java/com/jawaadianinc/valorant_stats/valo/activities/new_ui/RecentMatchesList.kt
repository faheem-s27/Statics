package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import AbilityCasts
import RecentMatchesDatabase
import ValorantMatch
import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.BuildConfig
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.Serializable
import java.net.URL
import java.util.concurrent.Executors
import kotlin.coroutines.CoroutineContext

data class AgentStats(val id: String, val timesPlayed: Int, val kills: Int, val deaths: Int, val assists: Int, val ability: AbilityCasts?) :
    Serializable

class RecentMatchesList : AppCompatActivity() {
    lateinit var gamemodeSpinner: Spinner
    lateinit var matchesSeekBar: SeekBar
    lateinit var processButton: Button
    lateinit var region: String
    lateinit var riotKey: String
    lateinit var scope: CoroutineScope
    var matchIdList = mutableListOf<String>()
    lateinit var AssetsDatabase: AssetsDatabase
    lateinit var mapsJSON: JSONArray
    lateinit var RecentMatchesDatabase: RecentMatchesDatabase
    private val characterCounts = mutableMapOf<String, Int>()
    private val agentsStatsMap = mutableMapOf<String, AgentStats>()
    private val mapCounts = mutableMapOf<String, Int>()
    private val rankCount = mutableMapOf<Int, Int>()
    private val playerCardsCount = mutableMapOf<String, Int>()

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recent_matches_list)

        region = intent.getStringExtra("region")!!
        riotKey = BuildConfig.RIOT_API_KEY
        scope = CoroutineScope(Dispatchers.IO)
        matchesSeekBar = findViewById(R.id.seekBar)
        processButton = findViewById(R.id.processMatchesButton)
        gamemodeSpinner = findViewById(R.id.gameModeSpinner)
        AssetsDatabase = AssetsDatabase(this)
        RecentMatchesDatabase = RecentMatchesDatabase(this)

        val gameModesList = listOf(
            "Competitive",
            "Unrated",
            "Spike Rush"
        )

        val adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, gameModesList)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gamemodeSpinner.adapter = adapter

        setButtonActive(false)

        scope.launch {
            val mapURL = "https://valorant-api.com/v1/maps"
            mapsJSON =
                JSONObject(URL(mapURL).readText()).getJSONArray("data")
        }

        gamemodeSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                getMatches(formatGameMode(gameModesList[position]), region)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }
        }

        matchesSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                // This method is called when the progress level changes.
                // 'progress' parameter holds the current progress value.
                // 'fromUser' parameter indicates whether the change was caused by the user or programmatically.
                if (fromUser)
                {
                    setButtonActive(true, text="Process $progress matches")
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                // This method is called when the user starts interacting with the SeekBar.
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                // This method is called when the user stops interacting with the SeekBar.
            }
        })

        processButton.setOnClickListener {
            val progressDialog = ProgressDialog(this)
            val totalMatches = matchesSeekBar.progress
            progressDialog.max = totalMatches
            progressDialog.setTitle("Processing Matches")
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
            progressDialog.show()

            var actualMatchesProcessed = 0

            // Create a custom coroutine dispatcher with a fixed thread pool
            val customDispatcher: CoroutineContext = Executors.newFixedThreadPool(400).asCoroutineDispatcher()


            scope.launch {
                val batchSize = 100 // Set the batch size according to your needs
                val totalBatches = totalMatches / batchSize + if (totalMatches % batchSize == 0) 0 else 1
                withContext(Main) {
                    progressDialog.max = batchSize
                }

                for (batchIndex in 0 until totalBatches) {
                    val startIndex = batchIndex * batchSize
                    val endIndex = minOf(startIndex + batchSize, totalMatches)

                    val coroutines = mutableListOf<Deferred<ValorantMatch?>>()

                    // Function to update progress based on completed coroutines
                    fun updateProgress() {
                        val completedMatches = coroutines.count { it.isCompleted }
                        val progressPercentage = (completedMatches.toFloat() / batchSize) * 100
                        updateProgressBar(
                            progressDialog,
                            progressPercentage.toInt(),
                            batchIndex,
                            totalBatches
                        )
                    }

                    for (i in startIndex until endIndex) {
                        val matchID = matchIdList[i]
                        val coroutine = CoroutineScope(customDispatcher).async {
                            getMatch2(matchID)
                        }
                        coroutine.invokeOnCompletion { updateProgress() } // Update progress on coroutine completion
                        coroutines.add(coroutine)
                    }

                    // Await all coroutines in this batch to be completed concurrently
                    coroutines.awaitAll()

                    // Process the matches in this batch once all the coroutines are done
                    for (coroutine in coroutines) {
                        val match = coroutine.getCompleted()
                        if (match != null) {
                            actualMatchesProcessed++
                            processMatch(match)
                        }
                    }
                }

                // All matches loaded!
                withContext(Main) {
                    progressDialog.dismiss()
                    Toast.makeText(this@RecentMatchesList, "Processed $actualMatchesProcessed matches!", Toast.LENGTH_SHORT).show()
                }

                val sortedCharacterCounts = characterCounts.toList().sortedByDescending { it.second }
                val sortedAgentsStats = agentsStatsMap.toList().sortedByDescending { it.second.timesPlayed }
                val sortedMapCounts = mapCounts.toList().sortedByDescending { it.second }
                val sortedCardCounts = playerCardsCount.toList().sortedByDescending { it.second }
                val sortedRankCount = rankCount.toList().sortedByDescending { it.second }

                val intent = android.content.Intent(
                    this@RecentMatchesList,
                    MatchesProcessedStats::class.java
                )
                intent.putExtra("agents", sortedAgentsStats.toTypedArray())
                intent.putExtra("maps", sortedMapCounts.toTypedArray())
                intent.putExtra("cards", sortedCardCounts.toTypedArray())
                intent.putExtra(
                    "Toolbar",
                    "$actualMatchesProcessed matches in ${gamemodeSpinner.selectedItem}"
                )
                startActivity(intent)
            }
        }
    }

    private fun processMatch(match: ValorantMatch) {
        try {
            val map = convertMapURL(match.matchInfo.mapId)
            mapCounts[map] = mapCounts.getOrDefault(map, 0) + 1
            for (player in match.players) {
                val character = player.characterId
                //characterCounts[character] = characterCounts.getOrDefault(character, 0) + 1

                val agentStatsObject = AgentStats(
                    character,
                    characterCounts.getOrDefault(character, 0) + 1,
                    player.stats.kills,
                    player.stats.deaths,
                    player.stats.assists,
                    player.stats.abilityCasts ?: AbilityCasts(0, 0, 0, 0)
                )

                if (agentsStatsMap.containsKey(agentStatsObject.id)) {
                    val existingStats = agentsStatsMap[agentStatsObject.id]!!
                    val newTimesPlayed = characterCounts.getOrDefault(character, 0) + 1
                    val newKills = existingStats.kills + agentStatsObject.kills
                    val newDeaths = existingStats.deaths + agentStatsObject.deaths
                    val newAssists = existingStats.assists + agentStatsObject.assists
                    val newAbilityCasts = AbilityCasts(
                        (existingStats.ability?.ability1Casts
                            ?: 0) + (agentStatsObject.ability?.ability1Casts
                            ?: 0),
                        (existingStats.ability?.ability2Casts
                            ?: 0) + (agentStatsObject.ability?.ability2Casts
                            ?: 0),
                        (existingStats.ability?.grenadeCasts
                            ?: 0) + (agentStatsObject.ability?.grenadeCasts
                            ?: 0),
                        (existingStats.ability?.ultimateCasts
                            ?: 0) + (agentStatsObject.ability?.ultimateCasts
                            ?: 0)
                    )
                    val updatedStats = AgentStats(
                        agentStatsObject.id,
                        newTimesPlayed,
                        newKills,
                        newDeaths,
                        newAssists,
                        newAbilityCasts
                    )

                    agentsStatsMap[agentStatsObject.id] = updatedStats
                } else {
                    agentsStatsMap[agentStatsObject.id] = agentStatsObject
                }

                val rank = player.competitiveTier
                val card = player.playerCard
                rankCount[rank] = rankCount.getOrDefault(rank, 0) + 1
                playerCardsCount[card] = playerCardsCount.getOrDefault(card, 0) + 1
            }
        } catch (e: Exception) {
            // Do nothing ??
        }
    }

    private fun updateProgressBar(view: ProgressDialog, progress: Int, batchIndex: Int, maxIndex: Int)
    {
        scope.launch(Main)
        {
            view.progress = progress
            view.setTitle("Processing Matches ($batchIndex/$maxIndex)")
        }
    }

    private fun convertMapURL(mapURL: String): String
    {
        for (i in 0 until mapsJSON.length())
        {
            if (mapsJSON.getJSONObject(i).getString("mapUrl") == mapURL)
            {
                return mapsJSON.getJSONObject(i).getString("uuid")
            }

        }
        return ""
    }

    private suspend fun getMatch(matchID: String) {
        if (!RecentMatchesDatabase.matchExists(matchID)) {
            val url =
                "https://$region.api.riotgames.com/val/match/v1/matches/$matchID?api_key=$riotKey"
            val json = JSONObject(URL(url).readText())
            try {
                //val match = Gson().fromJson(json.toString(), ValorantMatch::class.java)
                RecentMatchesDatabase.insertMatch2(json)
            } catch (_: Exception) {
            }
        }
    }

    private fun getMatch2(matchID: String): ValorantMatch? {
        val url =
            "https://$region.api.riotgames.com/val/match/v1/matches/$matchID?api_key=$riotKey"
        val json = JSONObject(URL(url).readText())
        return try {
            Gson().fromJson(json.toString(), ValorantMatch::class.java)
        } catch (_: Exception) {
            null
        }
    }

    fun formatGameMode(mode: String): String
    {
        return mode.replace(" ", "").lowercase()
    }

    private fun setButtonActive(isActive: Boolean, alphaValue: Float = 1f, text: String = "Loading") {
        processButton.alpha = if (isActive) alphaValue else 0.5f
        processButton.isEnabled = isActive
        processButton.text = text
    }

    fun getMatches(mode: String, region: String)
    {
        setButtonActive(false)
        val url = "https://$region.api.riotgames.com/val/match/v1/recent-matches/by-queue/$mode?api_key=$riotKey"
        scope.launch(Dispatchers.IO) {
            val json = JSONObject(URL(url).readText())
            val array = json.getJSONArray("matchIds")
            val length = array.length()
            RecentMatchesDatabase.deleteAllMatches()
            matchIdList.clear()
            for (i in 0 until array.length()) {
                val matchId = array.getString(i)
                matchIdList.add(matchId)
            }
            withContext(Main)
            {
                matchesSeekBar.max = length.coerceAtMost(5000)
                matchesSeekBar.progress = 1
                setButtonActive(true, text="Process 1 match")
            }
        }
    }
}
