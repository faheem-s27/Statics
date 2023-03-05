package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL
import java.util.*

class AssetsFragment : Fragment() {
    private lateinit var assetsTextView: TextView
    private lateinit var assets: ArrayList<String>
    private lateinit var assetListView: ListView
    private var TimerSeconds = 2

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
        assetListView = view.findViewById(R.id.new_AssetsListView)
        assetsTextView.text = "Assets"
        assets = ArrayList()
        assets.add("Agents")
        assets.add("Buddies")
        assets.add("Bundles")
        assets.add("Competitive Tiers")
        assets.add("Game Modes")
        assets.add("Level Borders")
        assets.add("Maps")
        assets.add("Player Cards")
        assets.add("Player Titles")
        assets.add("Sprays")
        assets.add("Weapons")
        getAssets(assets)

    }

    private fun getAssets(assets: ArrayList<String>) {
        val images = ArrayList<String>()

        doAsync {
            for (i in assets) {
                var imageString: String = ""
                // format i to be the correct format for the api, remove spaces and replace with no spaces and lowercase
                val formattedAssetName = i.replace(" ", "").lowercase()
                val URL = "https://valorant-api.com/v1/$formattedAssetName"

                val json = JSONObject(URL(URL).readText())
                if (json.getInt("status") != 200 || !json.has("data")) {
                    return@doAsync
                }

                val data = json.getJSONArray("data")
                // find every image in the data and add it to the images array
                for (j in 0 until data.length()) {
                    // check if displayIcon exists before adding it to the array
                    if (data.getJSONObject(j).has("displayIcon")) {
                        imageString += data.getJSONObject(j).getString("displayIcon") + " "
                    }
                }

                images.add(imageString)
            }

            uiThread {
                val adapter = AssetsButtonNewAdapter(this@AssetsFragment, assets, images)
                assetListView.adapter = adapter
                assetListView.setOnItemClickListener { _, _, position, _ ->
                    Toast.makeText(
                        requireActivity(),
                        "Clicked on ${assets[position]}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                //Toast.makeText(requireActivity(), "Assets Loaded", Toast.LENGTH_SHORT).show()
                changeText()
            }
        }
    }


    // Make a function that runs every 5 seconds that changes the text
    private fun changeText() {
        var index = 0
        val timer = Timer()
        timer.schedule(object : TimerTask() {
            override fun run() {
                activity?.runOnUiThread {
                    assetsTextView.setTextAnimation(assets[index])
                    index++
                    if (index == assets.size) {
                        index = 0
                    }
                }
            }
        }, 0, TimerSeconds * 1000L)
    }

    private fun TextView.setTextAnimation(
        text: String,
        duration: Long = 150,
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
        duration: Long = 150,
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

    private fun View.fadInAnimation(duration: Long = 150, completion: (() -> Unit)? = null) {
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