package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.RecentMatchStats.RecentMatchStatsAgentAdapter
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.agentValorantAPI.Agent
import com.jawaadianinc.valorant_stats.valo.databases.AssetsDatabase
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.net.URL


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
    lateinit var agentsCount: Array<Pair<String, AgentStats>>
    lateinit var mapsCount: Array<Pair<String, Int>>
    lateinit var assetsDB: AssetsDatabase
    lateinit var toolbar: MaterialToolbar
    private var agentsData: JSONObject? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_matches_processed_stats)
3
        agentsCount = (intent.getSerializableExtra("agents") as? Array<Pair<String, AgentStats>>)!!
        mapsCount = (intent.getSerializableExtra("maps") as? Array<Pair<String, Int>>)!!
        assetsDB = AssetsDatabase(this)
        toolbar = findViewById(R.id.materialToolbar4)

        toolbar.title = intent.getStringExtra("Toolbar") ?: "Recent Matches"

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

        val agentList = RecentMatchStatsAgentAdapter(agentsCount)
        val agentListView = findViewById<RecyclerView>(R.id.agent_recyclerView)
        agentListView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        agentListView.adapter = agentList

        val url = "https://valorant-api.com/v1/agents?isPlayableCharacter=true"
        GlobalScope.launch {
            agentsData = JSONObject(URL(url).readText())
        }

        val gradBg = findViewById<View>(R.id.gradientView2)
        var startingColours: GradientDrawable? = null


        val scrollListener = object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                // Calculate the selected item based on the scroll position
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                // Do something with the selected item
                //Toast.makeText(this@MatchesProcessedStats, "Selected ${agentsActualNames[firstVisibleItemPosition]}", Toast.LENGTH_SHORT).show()
                if (agentsData != null)
                {
                    for (i in 0 until agentsData!!.getJSONArray("data").length())
                    {
                        val agent = Gson().fromJson(agentsData!!.getJSONArray("data").getJSONObject(i).toString(), Agent::class.java)
                        if (agent.displayName == agentsActualNames[firstVisibleItemPosition])
                        {
                            val backgroundColours = agent.backgroundGradientColors
                            startingColours = if (gradBg.background != null) gradBg.background as GradientDrawable
                            else {
                                // int array of colours of grey
                                val greyColours = intArrayOf(
                                    Color.parseColor("#BDBDBD"),
                                    Color.parseColor("#BDBDBD"),
                                    Color.parseColor("#BDBDBD"),
                                    Color.parseColor("#BDBDBD")
                                )
                                GradientDrawable(
                                    GradientDrawable.Orientation.TOP_BOTTOM,
                                    greyColours
                                )
                            }
                            val startColours = startingColours!!.colors
                            val endColours = intArrayOf(
                                Color.parseColor(formatColour(backgroundColours[0])),
                                Color.parseColor(formatColour(backgroundColours[1])),
                                Color.parseColor(formatColour(backgroundColours[2])),
                                Color.parseColor(formatColour(backgroundColours[3]))
                            )

                            // Create a value animator for the color animation
                            val colorAnimator = ValueAnimator.ofFloat(0f, 1f)
                            colorAnimator.addUpdateListener { animator ->
                                val fraction = animator.animatedFraction
                                val colors = IntArray(startColours!!.size)
                                for (i in startColours.indices) {
                                    colors[i] =
                                        ArgbEvaluator().evaluate(fraction, startColours[i], endColours[i]) as Int
                                }
                                val gdView = GradientDrawable(
                                    GradientDrawable.Orientation.TOP_BOTTOM,
                                    colors
                                )
                                gdView.cornerRadius = 0f
                                gradBg.background = gdView
                            }
                            colorAnimator.duration = 500 // Set the duration of the animation in milliseconds
                            colorAnimator.start() // Start the color animation

                        }
                    }
                }
            }
        }

        agentListView.addOnScrollListener(scrollListener)

        // Set up the LinearSnapHelper
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(agentListView)


    }

    fun formatColour(colour: String): String
    {
        val new = colour.substring(0, colour.length - 2)
        return "#$new"
    }
}