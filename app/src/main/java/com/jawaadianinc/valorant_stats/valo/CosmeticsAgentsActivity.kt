package com.jawaadianinc.valorant_stats.valo

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.cosmetics.MySliderImageAdapter
import com.smarteist.autoimageslider.SliderView
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL

class CosmeticsAgentsActivity : AppCompatActivity() {
    private val bustPortrait = arrayListOf<String>()
    private val agentName = arrayListOf<String>()
    private val agentRole = arrayListOf<String>()
    private val roleDescription = arrayListOf<String>()
    private val roleIcon = arrayListOf<String>()
    private val agentDesc = arrayListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cosmetics_agents)
        loadAgentImage()
    }

    private fun setAgentInfo(position: Int) {
        findViewById<TextView>(R.id.AgentName).text = agentName[position]
    }

    private fun setImageInSlider(images: ArrayList<String>, imageSlider: SliderView) {
        val adapter = MySliderImageAdapter()
        adapter.renewItems(images)
        imageSlider.setSliderAdapter(adapter)
        findViewById<TextView>(R.id.AgentName).text = agentName[0]
        imageSlider.setCurrentPageListener {
            setAgentInfo(it)
        }
    }

    private fun loadAgentImage() {
        val imageSlider = findViewById<SliderView>(R.id.imageSlider)
        doAsync {
            val agentJSON =
                JSONObject(URL("https://valorant-api.com/v1/agents").readText()).getJSONArray("data")
            for (i in 0 until agentJSON.length()) {
                try {
                    bustPortrait.add(agentJSON.getJSONObject(i).getString("bustPortrait"))
                    agentName.add(agentJSON.getJSONObject(i).getString("displayName"))
                    agentRole.add(
                        agentJSON.getJSONObject(i).getJSONObject("role")
                            .getString("displayName")
                    )
                    roleDescription.add(
                        agentJSON.getJSONObject(i).getJSONObject("role")
                            .getString("description")
                    )
                    roleIcon.add(
                        agentJSON.getJSONObject(i).getJSONObject("role")
                            .getString("displayIcon")
                    )
                    agentDesc.add(agentJSON.getJSONObject(i).getString("description"))

                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            uiThread {
                setImageInSlider(bustPortrait, imageSlider)
            }
        }
    }
}
