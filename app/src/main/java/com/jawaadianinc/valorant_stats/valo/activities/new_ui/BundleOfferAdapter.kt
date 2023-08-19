package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso


class BundleOfferAdapter(
    private var context: Activity,
    private val weapons: ArrayList<BundleOffer>,
    private val bundleImage: String? = null,
    private val currency: String? = null
) :
    ArrayAdapter<Any?>(
        context, R.layout.weapon_skin_offer_layout, weapons as List<Any?>
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row =
            inflater.inflate(R.layout.weapon_skin_offer_layout, null, true)

        val weaponSkinOffer = weapons[position]
        val cost = row!!.findViewById<TextView>(R.id.weapon_skin_cost) as TextView
        val displayName = row.findViewById<TextView>(R.id.weapon_skin_title) as TextView
        val displayIcon = row.findViewById<ImageView>(R.id.weapon_skin_image) as ImageView
        val VPImage = row.findViewById<ImageView>(R.id.weapon_skin_VP) as ImageView

        // check if this is the last item in the list

        if (weaponSkinOffer.image.isNotBlank()) {
            Picasso
                .get()
                .load(weaponSkinOffer.image)
                .fit()
                .centerInside()
                .into(displayIcon)
        }
        displayName.text = weaponSkinOffer.name
        cost.text = weaponSkinOffer.price.toString()

        val layout = row.findViewById(R.id.background_image) as ImageView
        // Picasso load bundle image
        if (bundleImage != null) {
            Picasso
                .get()
                .load(bundleImage)
                .fit()
                .centerInside()
                .into(layout)
        }


//        else
//        {
//            layout.setBackgroundColor(context.resources.getColor(R.color.Valorant_SplashColourBackground))
//        }
        if (currency != null) {
            Picasso
                .get()
                .load("https://media.valorant-api.com/currencies/$currency/displayicon.png")
                .fit()
                .centerInside()
                .into(VPImage)
        } else {
            Picasso
                .get()
                .load("https://media.valorant-api.com/currencies/85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741/displayicon.png")
                .fit()
                .centerInside()
                .into(VPImage)
        }
        return row
    }
}
