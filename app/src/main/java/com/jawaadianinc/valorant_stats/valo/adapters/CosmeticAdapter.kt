package com.jawaadianinc.valorant_stats.valo.adapters

import android.app.Activity
import android.net.Uri
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toDrawable
import androidx.palette.graphics.Palette
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

            Picasso.get().load(this.image[position]).into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(
                    bitmap: android.graphics.Bitmap?,
                    from: Picasso.LoadedFrom?
                ) {
                    // get the average colour of the bitmap
                    val palette = Palette.from(bitmap!!).generate()
                    val color =
                        palette.getDominantColor(context.resources.getColor(R.color.Valorant_SplashColourBackground))

                    // if the colour is too light, set the text to black
                    if (color > -1) {
                        nameText.setTextColor(android.graphics.Color.BLACK)
                    }

                    imageView.setImageBitmap(bitmap)
                    row!!.background = color.toDrawable()
                }

                override fun onBitmapFailed(
                    e: java.lang.Exception?,
                    errorDrawable: android.graphics.drawable.Drawable?
                ) {
                }

                override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                }
            })

        } else if (cosmetic.lowercase(Locale.getDefault()) == "skins") {
            nameText!!.text = name[position]
            descText!!.text = ""
            imageView!!.layoutParams.height = 100
            imageView.layoutParams.width = 500
            nameText.textSize = 10f
            nameText.gravity = Gravity.CENTER
            Picasso.get().load(this.image[position]).into(object : com.squareup.picasso.Target {
                override fun onBitmapLoaded(
                    bitmap: android.graphics.Bitmap?,
                    from: Picasso.LoadedFrom?
                ) {
                    // get the average colour of the bitmap
                    val palette = Palette.from(bitmap!!).generate()
                    val color =
                        palette.getDominantColor(context.resources.getColor(R.color.Valorant_SplashColourBackground))
                    if (color > -1) {
                        nameText.setTextColor(android.graphics.Color.BLACK)
                    }
                    imageView.setImageBitmap(bitmap)
                    row!!.background = color.toDrawable()
                }

                override fun onBitmapFailed(
                    e: java.lang.Exception?,
                    errorDrawable: android.graphics.drawable.Drawable?
                ) {
                }

                override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                }
            })
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
        imageView?.animate()?.alpha(1f)?.translationXBy(200f)?.setDuration(1000)?.setInterpolator {
            val t = it - 1.0f
            t * t * t * t * t + 1.0f
        }?.start()

        nameText?.alpha = 0f
        nameText?.translationX = 200f
        nameText?.animate()?.alpha(1f)?.translationXBy(-200f)?.setDuration(1000)?.setInterpolator {
            val t = it - 1.0f
            t * t * t * t * t + 1.0f
        }?.start()

        return row!!
    }


}
