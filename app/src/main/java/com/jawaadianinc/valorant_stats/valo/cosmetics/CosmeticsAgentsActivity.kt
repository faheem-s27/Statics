package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.AgentAbilityAdapter
import com.jawaadianinc.valorant_stats.valo.adapters.MySliderImageAdapter
import com.smarteist.autoimageslider.SliderView
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class CosmeticsAgentsActivity : AppCompatActivity() {
    private val bustPortrait = arrayListOf<String>()
    private val agentName = arrayListOf<String>()
    private val agentRole = arrayListOf<String>()
    private val roleDescription = arrayListOf<String>()
    private val roleIcon = arrayListOf<String>()
    private val agentDesc = arrayListOf<String>()
    private val voiceLines = arrayListOf<String>()
    private val agentBackground = arrayListOf<String>()

    private lateinit var agentJSON: JSONArray

    private val animation = 200L
    private val agentVoicePlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cosmetics_agents)
        agentVoicePlayer.start()

        val progressDialog = ProgressDialogStatics().setProgressDialog(this, "Loading...")
        progressDialog.show()

        val data = intent.getStringExtra("data")
        if (data.toString().lowercase() == "agent") {
            loadAgentImage()
        } else {
            Toast.makeText(this, "No data for $data", Toast.LENGTH_SHORT).show()
        }


        progressDialog.dismiss()
    }

    private fun setAgentInfo(position: Int) {
        findViewById<TextView>(R.id.AgentName).setTextAnimation(agentName[position], animation)
        findViewById<TextView>(R.id.agentDescription).setTextAnimation(
            agentDesc[position],
            animation
        )
        findViewById<TextView>(R.id.roleName).setTextAnimation(agentRole[position], animation)
        findViewById<TextView>(R.id.roleDescription).setTextAnimation(
            roleDescription[position],
            animation
        )
        val roleImage: ImageView = findViewById(R.id.roleImage)
        Picasso.get().load(roleIcon[position]).fit().into(roleImage)
        doAsync {
            agentVoicePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            agentVoicePlayer.apply {
                setDataSource(voiceLines[position])
                prepare()
                start()
            }
        }
        val abilityNames = ArrayList<String>()
        val abilityDesc = ArrayList<String>()
        val abilityIcons = ArrayList<String>()

        var numba = position
        if (position >= 7) {
            numba += 1
        }

        for (i in 0 until agentJSON.getJSONObject(numba).getJSONArray("abilities").length()) {
            abilityNames += agentJSON.getJSONObject(numba).getJSONArray("abilities")
                .getJSONObject(i)
                .getString("displayName")
            abilityDesc += agentJSON.getJSONObject(numba).getJSONArray("abilities").getJSONObject(i)
                .getString("description")
            abilityIcons += agentJSON.getJSONObject(numba).getJSONArray("abilities")
                .getJSONObject(i)
                .getString("displayIcon")
        }

        val abilityList = AgentAbilityAdapter(this, abilityNames, abilityDesc, abilityIcons)
        val agentAbilityList = findViewById<ListView>(R.id.agentAbilityList)
        agentAbilityList.adapter = abilityList

    }

    private fun setImageInSlider(images: ArrayList<String>, imageSlider: SliderView) {
        val adapter = MySliderImageAdapter()
        adapter.renewItems(images)
        imageSlider.setSliderAdapter(adapter)
        setAgentInfo(0)
        imageSlider.setCurrentPageListener {
            agentVoicePlayer.reset()
            setAgentInfo(it)
        }
    }

    private fun loadAgentImage() {
        findViewById<TextView>(R.id.cosmeticTitle).setTextAnimation("vALORANT Agents", animation)

        val imageSlider = findViewById<SliderView>(R.id.imageSlider)
        doAsync {
            agentJSON =
                JSONObject(URL("https://valorant-api.com/v1/agents").readText()).getJSONArray(
                    "data"
                )
            for (i in 0 until agentJSON.length()) {
                try {
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
                    voiceLines.add(
                        agentJSON.getJSONObject(i).getJSONObject("voiceLine")
                            .getJSONArray("mediaList").getJSONObject(0).getString("wave")
                    )
                    bustPortrait.add(agentJSON.getJSONObject(i).getString("fullPortraitV2"))
                    agentBackground.add(agentJSON.getJSONObject(i).getString("bustPortrait"))
                    agentName.add(agentJSON.getJSONObject(i).getString("displayName"))
                } catch (e: Exception) {
                    Log.d("AgentCosmetics", e.toString())
                }
            }
            uiThread {
                setImageInSlider(bustPortrait, imageSlider)
            }
        }
    }

    private fun TextView.setTextAnimation(
        text: String,
        duration: Long = 300,
        completion: (() -> Unit)? = null
    ) {
        fadOutAnimation(duration) {
            this.text = text
            fadInAnimation(duration) {
                completion?.let {
                    it()
                }
            }
        }
    }

    private fun View.fadOutAnimation(
        duration: Long = 300,
        visibility: Int = View.INVISIBLE,
        completion: (() -> Unit)? = null
    ) {
        animate()
            .alpha(0f)
            .setDuration(duration)
            .withEndAction {
                this.visibility = visibility
                completion?.let {
                    it()
                }
            }
    }

    private fun View.fadInAnimation(duration: Long = 300, completion: (() -> Unit)? = null) {
        alpha = 0f
        visibility = View.VISIBLE
        animate()
            .alpha(1f)
            .setDuration(duration)
            .withEndAction {
                completion?.let {
                    it()
                }
            }
    }

}
