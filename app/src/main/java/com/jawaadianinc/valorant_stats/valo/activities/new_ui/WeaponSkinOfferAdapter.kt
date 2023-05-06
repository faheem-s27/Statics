package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso

class WeaponSkinOfferAdapter(
    private var context: Activity,
    private val weapons: ArrayList<WeaponSkinOffer>
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
        val RarityImage = row.findViewById<ImageView>(R.id.weapon_skin_rarity) as ImageView

        Picasso
            .get()
            .load(weaponSkinOffer.displayIcon)
            .fit()
            .centerInside()
            .into(displayIcon)

        displayName.text = weaponSkinOffer.displayName
        cost.text = weaponSkinOffer.cost.toString()

        val colour = weaponSkinOffer.rarity.getString("highlightColour")
        // remove the last two characters from the string
        var colourHex = colour.substring(0, colour.length - 2)
        colourHex = "#$colourHex"
        val layout = row.findViewById(R.id.background) as View
        layout.setBackgroundColor(android.graphics.Color.parseColor(colourHex))

        Picasso
            .get()
            .load(weaponSkinOffer.rarity.getString("displayIcon"))
            .fit()
            .centerInside()
            .into(RarityImage)

        Picasso
            .get()
            .load("https://media.valorant-api.com/currencies/85ad13f7-3d1b-5128-9eb2-7cd8ee0b5741/displayicon.png")
            .fit()
            .centerInside()
            .into(VPImage)

        return row
    }
}
