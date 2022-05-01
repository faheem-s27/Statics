package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.app.ProgressDialog
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jawaadianinc.valorant_stats.R
import com.smarteist.autoimageslider.SliderView
import com.squareup.picasso.Picasso
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
    private val voiceLines = arrayListOf<String>()
    private val agentBackground = arrayListOf<String>()
    private val animation = 200L
    private val mediaPlayer = MediaPlayer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cosmetics_agents)
        mediaPlayer.start()
        loadAgentImage()
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
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
            mediaPlayer.apply {
                setDataSource(voiceLines[position])
                prepare()
                start()
            }
        }
    }

    private fun setImageInSlider(images: ArrayList<String>, imageSlider: SliderView) {
        val adapter = MySliderImageAdapter()
        adapter.renewItems(images)
        imageSlider.setSliderAdapter(adapter)
        setAgentInfo(0)
        imageSlider.setCurrentPageListener {
            mediaPlayer.reset()
            setAgentInfo(it)
        }
    }

    private fun loadAgentImage() {
        val progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Loading")
        progressDialog.setMessage("Collecting Agent Data...")
        progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog.setCancelable(false)
        progressDialog.show()

        val imageSlider = findViewById<SliderView>(R.id.imageSlider)
        doAsync {
            val agentJSON =
                JSONObject(URL("https://valorant-api.com/v1/agents").readText()).getJSONArray("data")
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
                progressDialog.dismiss()
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