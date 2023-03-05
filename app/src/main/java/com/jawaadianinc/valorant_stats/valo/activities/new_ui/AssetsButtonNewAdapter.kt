package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class AssetsButtonNewAdapter(
    private val context: Fragment,
    private val assetName: ArrayList<String>,
    private val assetImages: ArrayList<String>
) : ArrayAdapter<Any>(
    context.requireActivity(), R.layout.cosmetic_layout, assetName as List<Any?>
) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row =
            inflater.inflate(R.layout.new_assets_button_layout, null, true)

        val assetNameTextView: Button? = row?.findViewById(R.id.new_AssetListButton)
        val assetImage: ImageView? = row?.findViewById(R.id.new_AssetListImage)

        // if the asset name has two words, then only show the second word
        if (assetName[position].contains(" ")) {
            val assetNameSplit = assetName[position].split(" ")
            assetNameTextView?.text = assetNameSplit[1]
        } else assetNameTextView?.text = assetName[position]

        val images = ArrayList<String>()

        val imageStrings = assetImages[position].split(" ")
        for (i in imageStrings) {
            if (i != "") {
                images.add(i)
            }
            //Log.d("AssetsButtonAdapter", "Got Asset Image: $i")
        }

        Log.d("AssetsButtonAdapter", "Got Asset Images for ${assetName[position]}: ${images.size}")
        if (images.isNotEmpty()) {
            images.shuffle()

            // a smooth fade in for the new image with Picasso


            Picasso
                .get()
                .load(images[0])
                .placeholder(assetImage!!.drawable)
                .fit()
                .centerInside()
                .into(assetImage, object : com.squareup.picasso.Callback {
                    override fun onSuccess() {
//                        assetImage.alpha = 0f
//                          assetImage.animate().setDuration(300).alpha(1f).start()

                    }

                    override fun onError(e: Exception?) {
                        Log.d(
                            "AssetsButtonAdapter",
                            "Failed to load image for ${assetName[position]}"
                        )
                    }
                })
        }

        return row!!

    }
}
