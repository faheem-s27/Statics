package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.media.AudioManager
import android.media.MediaPlayer
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.storage.StorageReference
import com.jawaadianinc.valorant_stats.R
import org.jetbrains.anko.doAsync

class VoiceLineAdapter(
    private val context: Activity,
    private val VoiceLines: ArrayList<String>,
    private val storageRef: StorageReference,
    private val agentName: String,
    private val voiceLinePlayer: MediaPlayer
) : ArrayAdapter<Any?>(
    context, R.layout.voice_line, VoiceLines as List<Any?>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.voice_line, null, true)

        val playFAB = row!!.findViewById<FloatingActionButton>(R.id.playVoiceLineFAB)
        val name = row.findViewById<TextView>(R.id.voiceLinesName)

        // remove the .mp3 from the name
        val editedName = VoiceLines[position].replace(".mp3", "")
        // put a space between capital letters
        val spacedName = editedName.replace("(?<!^)(?=[A-Z])".toRegex(), " ")
        // add a space between letters and numbers
        val spacedName2 = spacedName.replace("(?<=\\D)(?=\\d)".toRegex(), " ")
        name.text = spacedName2

        val listRef = storageRef.child("Valorant VoiceLines/${agentName}/")

        playFAB.setOnClickListener {
            val fileRef = listRef.child(VoiceLines[position])
            fileRef.downloadUrl.addOnSuccessListener { uri ->
                // set the voice line player to the uri
                voiceLinePlayer.reset()
                doAsync {
                    try {
                        voiceLinePlayer.setAudioStreamType(AudioManager.STREAM_MUSIC)
                        voiceLinePlayer.apply {
                            setDataSource(uri.toString())
                            prepare()
                            start()
                        }

                        // when the voice line is finished, reset the player
                        voiceLinePlayer.setOnCompletionListener {
                            voiceLinePlayer.reset()
                        }

                    } catch (e: Exception) {
                        Log.e("VoiceLine", "Error: ${e.message}")
                    }
                }
            }
        }

        // animate them coming in from the side
        row.translationX = -1000f
        row.animate().translationXBy(1000f).setDuration(500).setInterpolator {
            val t = it - 1.0f
            t * t * t * t * t + 1.0f
        }.start()

        return row
    }
}
