package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.graphics.Color.parseColor
import android.graphics.drawable.GradientDrawable
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
import com.google.firebase.storage.FirebaseStorage
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.AgentAbilityAdapter
import com.jawaadianinc.valorant_stats.valo.adapters.MySliderImageAdapter
import com.jawaadianinc.valorant_stats.valo.adapters.VoiceLineAdapter
import com.smarteist.autoimageslider.SliderView
import com.squareup.picasso.Picasso
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class CosmeticsAgentsActivity : AppCompatActivity() {
    private val bustPortrait = arrayListOf<String>()
    private val agentTextBackgrounds = arrayListOf<String>()
    private val agentName = arrayListOf<String>()
    private val agentRole = arrayListOf<String>()
    private val roleDescription = arrayListOf<String>()
    private val roleIcon = arrayListOf<String>()
    private val agentDesc = arrayListOf<String>()
    private val voiceLines = arrayListOf<String>()
    private val agentBackground = arrayListOf<String>()
    private val backgroundGradientColors = arrayListOf<String>()

    private lateinit var agentJSON: JSONArray

    private val animation = 200L
    private val agentVoicePlayer = MediaPlayer()
    private val voiceLinePlayer = MediaPlayer()
    private val storageRef = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_cosmetics_agents)
        agentVoicePlayer.start()
        voiceLinePlayer.start()

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

        try {
            val gradBg = findViewById<View>(R.id.gradientView)
            val backgroundColours = backgroundGradientColors[position].split(",")
            val gdView = GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(
                    parseColor(backgroundColours[0]),
                    parseColor(backgroundColours[1]),
                    parseColor(backgroundColours[2]),
                    parseColor(backgroundColours[3])
                )
            )
            gdView.cornerRadius = 0f
            gradBg.background = gdView
        } catch (e: Exception) {
            // set background to null
            val gradBg = findViewById<View>(R.id.gradientView)
            gradBg.background = null
            Log.e("Gradient", "Error: ${e.message}")
        }

        for (i in 0 until agentJSON.getJSONObject(position).getJSONArray("abilities").length()) {
            abilityNames += agentJSON.getJSONObject(position).getJSONArray("abilities")
                .getJSONObject(i)
                .getString("displayName")
            abilityDesc += agentJSON.getJSONObject(position).getJSONArray("abilities")
                .getJSONObject(i)
                .getString("description")
            abilityIcons += agentJSON.getJSONObject(position).getJSONArray("abilities")
                .getJSONObject(i)
                .getString("displayIcon")
        }

        val abilityList = AgentAbilityAdapter(this, abilityNames, abilityDesc, abilityIcons)
        val agentAbilityList = findViewById<ListView>(R.id.agentAbilityList)
        agentAbilityList.adapter = abilityList

        val voiceLinesListView = findViewById<ListView>(R.id.voiceLines)
        val listRef = storageRef.child("Valorant VoiceLines/${agentName[position]}/")
        // text adapter list/
        val voiceLinesArray = ArrayList<String>()
        val voiceLinesLinks = ArrayList<String>()

        val voiceLineAdapter = VoiceLineAdapter(
            this,
            voiceLinesArray,
            storageRef,
            agentName[position],
            voiceLinePlayer
        )
        voiceLinesListView.adapter = voiceLineAdapter

        listRef.listAll().addOnSuccessListener { listResult ->
            listResult.items.forEach { item ->
                voiceLinesArray.add(item.name)
                voiceLineAdapter.notifyDataSetChanged()
            }
        }
    }

    private fun setImageInSlider(
        images: ArrayList<String>,
        imageSlider: SliderView,
        textBackground: ArrayList<String>,
        imageSliderText: SliderView
    ) {
        val adapter = MySliderImageAdapter()
        val adapterText = MySliderImageAdapter()
        adapter.renewItems(images)
        adapterText.renewItems(textBackground)
        imageSlider.setSliderAdapter(adapter)
        imageSliderText.setSliderAdapter(adapterText)
        setAgentInfo(0)

        imageSlider.setCurrentPageListener {
            // Set the slider text background
            imageSliderText.currentPagePosition = it

            agentVoicePlayer.reset()
            voiceLinePlayer.reset()
            setAgentInfo(it)
        }
    }

    private fun loadAgentImage() {
        val imageSlider = findViewById<SliderView>(R.id.imageSlider)
        doAsync {
            agentJSON =
                JSONObject(URL("https://valorant-api.com/v1/agents?isPlayableCharacter=true").readText()).getJSONArray(
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
                    bustPortrait.add(agentJSON.getJSONObject(i).getString("fullPortrait"))
                    agentTextBackgrounds.add(agentJSON.getJSONObject(i).getString("background"))
                    agentBackground.add(agentJSON.getJSONObject(i).getString("bustPortrait"))

                    val name = agentJSON.getJSONObject(i).getString("displayName")
                    // replace any / with -
                    agentName.add(name.replace("/", "-"))

                    // the background gradient colors are an array of hex colours for each agent
                    // the first colour is the top colour and the second colour is the bottom colour
                    val backgroundGradientColorsJSONArray =
                        agentJSON.getJSONObject(i).getJSONArray("backgroundGradientColors")
                    // iterate through the array and add each colour to the arraylist
                    var backgroundColourGradientString = ""
                    for (j in 0 until backgroundGradientColorsJSONArray.length()) {
                        val currentColour = backgroundGradientColorsJSONArray.getString(j)
                        // remove the 2 "ff" from the end of the colour
                        val colour = currentColour.substring(0, currentColour.length - 2)
                        // add a hash at the start
                        val colourWithHash = "#$colour"
                        // add the colour to the string
                        backgroundColourGradientString += "$colourWithHash,"
                    }
                    // remove the last comma
                    backgroundColourGradientString = backgroundColourGradientString.substring(
                        0,
                        backgroundColourGradientString.length - 1
                    )
                    // add the string to the arraylist
                    backgroundGradientColors.add(backgroundColourGradientString)
                    Log.d("backgroundGradientColors", backgroundGradientColors.toString())

                } catch (e: Exception) {
                    Log.d("AgentCosmetics", e.toString())
                }
            }
            uiThread {
                setImageInSlider(
                    bustPortrait,
                    imageSlider,
                    agentTextBackgrounds,
                    findViewById(R.id.imageSliderText)
                )
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
