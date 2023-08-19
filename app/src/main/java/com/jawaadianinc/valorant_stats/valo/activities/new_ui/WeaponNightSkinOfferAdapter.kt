package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.app.Activity
import android.graphics.Paint
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso


class WeaponNightSkinOfferAdapter(
    private var context: Activity,
    private val weapons: ArrayList<NightWeaponSkinOffer>
) :
    ArrayAdapter<Any?>(
        context, R.layout.night_weapon_skin_offer_layout, weapons as List<Any?>
    ) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row =
            inflater.inflate(R.layout.night_weapon_skin_offer_layout, null, true)

        val weaponSkinOffer = weapons[position]
        val cost = row!!.findViewById(R.id.weapon_skin_cost_original) as TextView
        val discountedCostText = row.findViewById<TextView>(R.id.weapon_skin_cost_discount)
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
        cost.paintFlags = cost.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        discountedCostText.text = weaponSkinOffer.discountCost.toString()

        val colour = weaponSkinOffer.rarity.getString("highlightColour")
        var colourHex = colour.substring(0, colour.length - 2)
        colourHex = darkenColor(colourHex)

        val layout = row.findViewById(R.id.playerStoreListView_circle) as View
        layout.setBackgroundColor(android.graphics.Color.parseColor(colourHex))

        VPImage.setColorFilter(
            android.graphics.Color.parseColor(
                "#${
                    colour.substring(
                        0,
                        colour.length - 2
                    )
                }"
            )
        )

        // make displayname colour the same colour
        displayName.setTextColor(
            android.graphics.Color.parseColor(
                "#${
                    colour.substring(
                        0,
                        colour.length - 2
                    )
                }"
            )
        )
        cost.setTextColor(
            android.graphics.Color.parseColor(
                "#${
                    colour.substring(
                        0,
                        colour.length - 2
                    )
                }"
            )
        )

        discountedCostText.setTextColor(
            android.graphics.Color.parseColor(
                "#${
                    colour.substring(
                        0,
                        colour.length - 2
                    )
                }"
            )
        )

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

    private fun darkenColor(hexColor: String): String {
        // Convert hex color to RGB values
        val red = hexColor.substring(0, 2).toInt(16)
        val green = hexColor.substring(2, 4).toInt(16)
        val blue = hexColor.substring(4, 6).toInt(16)

        // Shift each color component towards black by 75%
        val darkerRed = (red * 0.50).toInt()
        val darkerGreen = (green * 0.50).toInt()
        val darkerBlue = (blue * 0.50).toInt()

        // Convert the darker RGB values back to hex
        return String.format("#%02X%02X%02X", darkerRed, darkerGreen, darkerBlue)
    }
}
