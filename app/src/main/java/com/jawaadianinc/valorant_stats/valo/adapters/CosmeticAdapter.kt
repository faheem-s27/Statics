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

        val nameText = row?.findViewById(R.id.agentAbilityTitle) as TextView?
        val descText = row?.findViewById(R.id.agentAbilityDescription) as TextView?
        val imageView = row?.findViewById(R.id.agentAbilityIcon) as ImageView?

        if (cosmetic.lowercase(Locale.getDefault()) == "weapon") {
            nameText!!.text = name[position]
            descText!!.text = ""

            imageView!!.layoutParams.height = 200
            imageView.layoutParams.width = 400
            nameText.textSize = 20f
            Picasso.get().load(this.image[position]).fit().centerInside().into(imageView)

        } else if (cosmetic.lowercase(Locale.getDefault()) == "skins") {
            nameText!!.text = name[position]
            descText!!.text = ""
            imageView!!.layoutParams.height = 100
            imageView.layoutParams.width = 500
            nameText.textSize = 10f
            nameText.gravity = Gravity.CENTER
            Picasso.get().load(this.image[position]).fit().centerInside().into(imageView)
        } else if (cosmetic.lowercase(Locale.getDefault()) == "videos") {
            // make the video layout
            val videoView = row!!.findViewById(R.id.videoView2) as VideoView
            videoView.setVideoURI(Uri.parse(this.image[position]))
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
        imageView?.alpha = 0f
        // move them 200 pixels to the right
        imageView?.translationX = -200f
        imageView?.animate()?.alpha(1f)?.translationXBy(200f)?.setDuration(1000)?.start()

        nameText?.alpha = 0f
        nameText?.translationX = 200f
        nameText?.animate()?.alpha(1f)?.translationXBy(-200f)?.setDuration(1000)?.start()

        return row!!
    }


}
