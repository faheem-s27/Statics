package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.jetbrains.anko.uiThread
import org.json.JSONObject
import java.net.URL
import java.util.Timer
import java.util.TimerTask

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
        assets.add(getString(R.string.s79))
        assets.add(getString(R.string.s80))
        assets.add(getString(R.string.s82))
        assets.add(getString(R.string.s84))
        assets.add(getString(R.string.s85))
        assets.add(getString(R.string.s86))
        assets.add(getString(R.string.s88))
        assets.add(getString(R.string.s89))
        getAssets(assets)

        val bg = view.findViewById<ImageView>(R.id.new_AssetsBackground)
        Picasso.get().load(StaticsMainActivity.playerCardLarge).fit().centerCrop()
            .transform(BlurTransformation(requireContext())).into(bg)

    }

    private fun getAssets(assets: ArrayList<String>) {
        val images = ArrayList<String>()

        val assetsList = ArrayList<String>()
        assetsList.add("agents")
        assetsList.add("buddies")
        assetsList.add("competitivetiers")
        assetsList.add("levelborders")
        assetsList.add("maps")
        assetsList.add("playercards")
        assetsList.add("sprays")
        assetsList.add("weapons")

        doAsync {
            for (i in assetsList) {
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
                }
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
