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

        assetNameTextView?.text = assetName[position]

        val images = ArrayList<String>()

        val imageStrings = assetImages[position].split(" ")
        for (i in imageStrings) {
            if (i != "") {
                images.add(i)
            }
        }

        assetNameTextView?.setOnClickListener {
            when (position) {
                0 -> {
                    val intent =
                        Intent(context.requireActivity(), CosmeticsAgentsActivity::class.java)
                    intent.putExtra("data", "agent")
                    context.requireActivity().startActivity(intent)
                    context.requireActivity()
                        .overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }

                1 -> {
                    val intent =
                        Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                    intent.putExtra("cosmetic", "buddies")
                    context.requireActivity().startActivity(intent)
                    context.requireActivity()
                        .overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }

                2 -> {
                    val intent =
                        Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                    intent.putExtra("cosmetic", "ranks")
                    context.requireActivity().startActivity(intent)
                    context.requireActivity()
                        .overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }

                4 -> {
                    val intent =
                        Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                    intent.putExtra("cosmetic", "maps")
                    context.requireActivity().startActivity(intent)
                    context.requireActivity()
                        .overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }

                3 -> {
                    val intent =
                        Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                    intent.putExtra("cosmetic", "borders")
                    context.requireActivity().startActivity(intent)
                    context.requireActivity()
                        .overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }

                5 -> {
                    val intent =
                        Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                    intent.putExtra("cosmetic", "cards")
                    intent.putExtra("size", "large")
                    context.requireActivity().startActivity(intent)
                    context.requireActivity()
                        .overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }

                7 -> {
                    val intent =
                        Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                    intent.putExtra("cosmetic", "weapons")
                    context.requireActivity().startActivity(intent)
                    context.requireActivity()
                        .overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }

                6 -> {
                    val intent =
                        Intent(context.requireActivity(), CosmeticsListActivity::class.java)
                    intent.putExtra("cosmetic", "sprays")
                    context.requireActivity().startActivity(intent)
                    context.requireActivity()
                        .overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                }
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
