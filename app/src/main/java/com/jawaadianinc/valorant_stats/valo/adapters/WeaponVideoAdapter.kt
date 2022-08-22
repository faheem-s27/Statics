package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.net.Uri
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jawaadianinc.valorant_stats.R


class WeaponVideoAdapter(
    private val context: Activity,
    private val name: ArrayList<String>,
    private val image: ArrayList<String>,
) : ArrayAdapter<Any>(context, R.layout.cosmetic_layout, name as List<Any>) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row =
            inflater.inflate(R.layout.weaponskinvideo_layout, parent, false)
        val videoView = row!!.findViewById(R.id.videoView2) as VideoView

        val playFAB = row.findViewById(R.id.playFAB) as FloatingActionButton
        val loading = row.findViewById(R.id.loadingVid) as ProgressBar
        val title = row.findViewById(R.id.videoTitle) as TextView
        val loadButton = row.findViewById(R.id.loadVid) as Button
        loading.visibility = View.INVISIBLE
        title.text = name[position]
        playFAB.visibility = View.INVISIBLE
        loadButton.visibility = View.VISIBLE
        loadButton.setOnClickListener {
            loading.visibility = View.VISIBLE
            videoView.setVideoURI(Uri.parse(image[position]))
            title.text = "Loading..."
            loadButton.visibility = View.INVISIBLE
        }

        videoView.setOnPreparedListener {
            it.isLooping = true
            title.text = name[position]
            playFAB.visibility = View.VISIBLE
            loading.visibility = View.INVISIBLE
        }
        // check if video has played
        videoView.setOnCompletionListener {
            // start playing again from the start
            Toast.makeText(context, "Video has ended", Toast.LENGTH_SHORT).show()
        }

        playFAB.setOnClickListener {
            if (videoView.isPlaying) {
                videoView.pause()
                title.text = "Paused"
                playFAB.setImageResource(R.drawable.pause)
            } else {
                videoView.start()
                title.text = name[position]
                playFAB.setImageResource(R.drawable.play_button_arrowhead)
            }
        }

        return row
    }


}
