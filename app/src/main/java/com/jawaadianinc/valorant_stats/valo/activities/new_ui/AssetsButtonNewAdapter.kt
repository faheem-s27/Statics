package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.Intent
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsAgentsActivity
import com.jawaadianinc.valorant_stats.valo.cosmetics.CosmeticsListActivity
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

        assetNameTextView?.setOnClickListener {
            if (assetName[position] == "Agents") {
                val intent = Intent(context.requireActivity(), CosmeticsAgentsActivity::class.java)
                intent.putExtra("data", "agent")
                context.requireActivity().startActivity(intent)
                context.requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            } else if (assetName[position] == "Buddies") {
                val intent = Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", "buddies")
                context.requireActivity().startActivity(intent)
                context.requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            } else if (assetName[position] == "Competitive Tiers") {
                val intent = Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", "ranks")
                context.requireActivity().startActivity(intent)
                context.requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }
            // do the same for "Maps", "Level Borders", "Player Cards", "Weapons"
            else if (assetName[position] == "Maps") {
                val intent = Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", "maps")
                context.requireActivity().startActivity(intent)
                context.requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            } else if (assetName[position] == "Level Borders") {
                val intent = Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", "borders")
                context.requireActivity().startActivity(intent)
                context.requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            } else if (assetName[position] == "Player Cards") {
                val intent = Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", "cards")
                intent.putExtra("size", "large")
                context.requireActivity().startActivity(intent)
                context.requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            } else if (assetName[position] == "Weapons") {
                val intent = Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", "weapons")
                context.requireActivity().startActivity(intent)
                context.requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            } else if (assetName[position] == "Sprays") {
                val intent = Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                intent.putExtra("cosmetic", "sprays")
                context.requireActivity().startActivity(intent)
                context.requireActivity().overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            }
        }

        if (images.isNotEmpty()) {
            images.shuffle()
            Picasso
                .get()
                .load(images[0])
                .placeholder(assetImage!!.drawable)
                .into(object : com.squareup.picasso.Target {
                    override fun onBitmapLoaded(
                        bitmap: android.graphics.Bitmap?,
                        from: Picasso.LoadedFrom?
                    ) {
                        // fade in the new image
                        assetImage.animate().alpha(0f).setDuration(200).withEndAction {
                            assetImage.setImageBitmap(bitmap)
                            assetImage.animate().alpha(1f).setDuration(200).start()
                        }
                    }

                    override fun onBitmapFailed(
                        e: java.lang.Exception?,
                        errorDrawable: android.graphics.drawable.Drawable?
                    ) {
                        Log.d("AssetsButtonAdapter", "Failed to load image")
                    }

                    override fun onPrepareLoad(placeHolderDrawable: android.graphics.drawable.Drawable?) {
                        Log.d("AssetsButtonAdapter", "Preparing to load image")
                    }
                })
        }

        return row!!

    }
}
