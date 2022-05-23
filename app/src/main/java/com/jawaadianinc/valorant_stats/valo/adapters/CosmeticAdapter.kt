package com.jawaadianinc.valorant_stats.valo.cosmetics

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import java.util.*


class CosmeticAdapter(
    private val context: Activity,
    private val cosmetic: String,
    private val name: ArrayList<String>,
    private val image: ArrayList<String>,
) : ArrayAdapter<Any>(context, R.layout.cosmetic_layout, name as List<Any>) {

    var isImageFitToScreen = false


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row = inflater.inflate(R.layout.cosmetic_layout, null, true)
        val NameText = row!!.findViewById(R.id.agentAbilityTitle) as TextView
        val DescText = row.findViewById(R.id.agentAbilityDescription) as TextView
        val ImageView = row.findViewById(R.id.agentAbilityIcon) as ImageView

        if (cosmetic.lowercase(Locale.getDefault()) == "weapon") {
            NameText.text = name[position]
            DescText.text = ""

            ImageView.layoutParams.height = 200
            ImageView.layoutParams.width = 400
            NameText.textSize = 20f

            Picasso.get().load(image[position]).fit().centerInside().into(ImageView)
        } else if (cosmetic.lowercase(Locale.getDefault()) == "skins") {
            NameText.text = name[position]
            DescText.text = ""
            ImageView.layoutParams.height = 100
            ImageView.layoutParams.width = 500
            NameText.textSize = 10f
            Picasso.get().load(image[position]).fit().centerInside().into(ImageView)
        }

        // animate the image from 0 alpha to 1 alpha
        ImageView.alpha = 0f
        ImageView.animate().alpha(1f).setDuration(1000).start()

        return row
    }


}

