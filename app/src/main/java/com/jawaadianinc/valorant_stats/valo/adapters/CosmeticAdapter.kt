package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import java.util.*


class CosmeticAdapter(
    private val context: Activity,
    private val cosmetic: String,
    private val name: ArrayList<String>,
    private val image: ArrayList<String>,
) : ArrayAdapter<Any>(context, R.layout.cosmetic_layout, name as List<Any>) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) {
            row = if (cosmetic != "videos") {
                inflater.inflate(R.layout.cosmetic_layout, parent, false)
            } else {
                inflater.inflate(R.layout.weaponskinvideo_layout, parent, false)
            }
        }

        val NameText = row?.findViewById(R.id.agentAbilityTitle) as TextView?
        val DescText = row?.findViewById(R.id.agentAbilityDescription) as TextView?
        val ImageView = row?.findViewById(R.id.agentAbilityIcon) as ImageView?

        if (cosmetic.lowercase(Locale.getDefault()) == "weapon") {
            NameText!!.text = name[position]
            DescText!!.text = ""

            ImageView!!.layoutParams.height = 200
            ImageView.layoutParams.width = 400
            NameText.textSize = 20f
            Picasso.get().load(image[position]).fit().centerInside().into(ImageView)

        } else if (cosmetic.lowercase(Locale.getDefault()) == "skins") {
            NameText!!.text = name[position]
            DescText!!.text = ""
            ImageView!!.layoutParams.height = 100
            ImageView.layoutParams.width = 500
            NameText.textSize = 10f
            NameText.gravity = Gravity.CENTER
            Picasso.get().load(image[position]).fit().centerInside().into(ImageView)
        } else if (cosmetic.lowercase(Locale.getDefault()) == "videos") {
            // make the video layout
            val videoView = row!!.findViewById(R.id.videoView2) as VideoView
            videoView.setVideoURI(Uri.parse(image[position]))
            val playFAB = row.findViewById(R.id.playFAB) as FloatingActionButton
            val loading = row.findViewById(R.id.loadingVid) as ProgressBar
            val title = row.findViewById(R.id.videoTitle) as TextView
            playFAB.visibility = View.INVISIBLE
            loading.visibility = View.VISIBLE
            title.text = name[position]
            videoView.setOnPreparedListener {
                playFAB.visibility = View.VISIBLE
                loading.visibility = View.INVISIBLE
            }

            playFAB.setOnClickListener {
                if (videoView.isPlaying) {
                    videoView.pause()
                    playFAB.setImageResource(R.drawable.pause)
                } else {
                    videoView.start()
                    playFAB.setImageResource(R.drawable.play_button_arrowhead)
                }
            }
        }

        // animate the image from 0 alpha to 1 alpha
        ImageView?.alpha = 0f
        // move them 200 pixels to the right
        ImageView?.translationX = -200f
        ImageView?.animate()?.alpha(1f)?.translationXBy(200f)?.setDuration(1000)?.start()

        NameText?.alpha = 0f
        NameText?.translationX = 200f
        NameText?.animate()?.alpha(1f)?.translationXBy(-200f)?.setDuration(1000)?.start()

        return row!!
    }


}
