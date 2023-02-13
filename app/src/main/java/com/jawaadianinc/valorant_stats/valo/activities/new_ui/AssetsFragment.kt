package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R

class AssetsFragment : Fragment() {
    private lateinit var assetsTextView: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_assets, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assetsTextView = view.findViewById(R.id.new_AssetsText)
        assetsTextView.text = "Assets"

        // have an array of valorant assets like agents weapons  maps
        // and then have a for loop that goes through the array and sets the text to the next item in the array
        val assets = arrayOf("Agents", "Weapons", "Maps")
        var index = 0
        assetsTextView.setOnClickListener {
            assetsTextView.setTextAnimation(assets[index])
            index++
            if (index == assets.size) {
                index = 0
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
        // move the text view to the top of the screen
        visibility = View.VISIBLE
        translationY = -100f
        animate()
            .alpha(1f)
            .setDuration(duration)
            .translationYBy(100f)
            .setInterpolator(android.view.animation.AccelerateDecelerateInterpolator())
            .withEndAction {
                completion?.let {
                    it()
                }
            }
    }

}
