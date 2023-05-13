package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
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
import com.google.gson.Gson
import com.jawaadianinc.valorant_stats.ProgressDialogStatics
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.activities.new_ui.agentValorantAPI.Agent
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

        val gradBg = findViewById<View>(R.id.gradientView)
        val backgroundColours = backgroundGradientColors[position].split(",")
        var startingColours: GradientDrawable? = null
        startingColours = if (gradBg.background != null) gradBg.background as GradientDrawable
        else {
            // int array of colours of grey
            val greyColours = intArrayOf(
                parseColor("#BDBDBD"),
                parseColor("#BDBDBD"),
                parseColor("#BDBDBD"),
                parseColor("#BDBDBD")
            )
            GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                greyColours
            )
        }
        val startColours = startingColours.colors
        val endColours = intArrayOf(
            parseColor(backgroundColours[0]),
            parseColor(backgroundColours[1]),
            parseColor(backgroundColours[2]),
            parseColor(backgroundColours[3])
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

        val agent = Gson().fromJson(agentJSON.getJSONObject(position).toString(), Agent::class.java)

        val abilityNames = ArrayList<String>()
        val abilityDesc = ArrayList<String>()
        val abilityIcons = ArrayList<String>()

        for (ability in agent.abilities) {
            abilityNames.add(ability.displayName)
            abilityDesc.add(ability.description)
            abilityIcons.add(ability.displayIcon)
        }

        val abilityList = AgentAbilityAdapter(this, abilityNames, abilityDesc, abilityIcons)
        val agentAbilityList = findViewById<ListView>(R.id.agentAbilityList)
        agentAbilityList.adapter = abilityList

        val voiceLinesListView = findViewById<ListView>(R.id.voiceLines)
        val listRef = storageRef.child("Valorant VoiceLines/${agentName[position]}/")
        val voiceLinesArray = ArrayList<String>()
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
        // get the language from shared preferences
        val sharedPref = getSharedPreferences("UserLocale", MODE_PRIVATE)
        val LANGUAGE = sharedPref.getString("locale", "").toString()
        //Toast.makeText(this, LANGUAGE, Toast.LENGTH_SHORT).show()
        val url = "https://valorant-api.com/v1/agents?isPlayableCharacter=true&language=$LANGUAGE"
        val imageSlider = findViewById<SliderView>(R.id.imageSlider)
        doAsync {
            agentJSON =
                JSONObject(URL(url).readText()).getJSONArray(
                    "data"
                )
            for (i in 0 until agentJSON.length()) {
                val currentAgent = agentJSON.getJSONObject(i)
                val agent = Gson().fromJson(currentAgent.toString(), Agent::class.java)
                agentRole.add(agent.role.displayName)
                Log.d("Agent", agent.role.displayName)
                roleDescription.add(agent.role.description)
                Log.d("Agent", agent.role.description)
                roleIcon.add(agent.role.displayIcon)
                Log.d("Agent", agent.role.displayIcon)
                agentDesc.add(agent.description)
                Log.d("Agent", agent.description)
                // check if the agent voice line is null
                if (!currentAgent.isNull("voiceLine")) {
                    voiceLines.add(agent.voiceLine.mediaList[0].wave)
                    Log.d("Agent", agent.voiceLine.mediaList[0].wave)
                } else {
                    voiceLines.add("")
                    Log.d("Agent", "EMPTY")
                }
                bustPortrait.add(agent.fullPortrait)
                Log.d("Agent", agent.fullPortrait)
                agentTextBackgrounds.add(agent.background)
                Log.d("Agent", agent.background)
                agentBackground.add(agent.bustPortrait)
                Log.d("Agent", agent.bustPortrait)
                agentName.add(agent.displayName.replace("/", "-"))
                Log.d("Agent", agent.displayName.replace("/", "-"))

                val backgroundColours = agent.backgroundGradientColors
                var backgroundColourGradientString = ""
                for (element in backgroundColours) {
                    // remove the 2 "ff" from the end of the colour
                    val colour = element.substring(0, element.length - 2)
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
                backgroundGradientColors.add(backgroundColourGradientString)
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
