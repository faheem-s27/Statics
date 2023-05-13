package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R

class AssetsButtonAdapter(
    private val context: Fragment,
    private val assetName: Array<String>,
    private val assetImages: ArrayList<String>
) : ArrayAdapter<Any>(
    context.requireActivity(),
    R.layout.cosmetic_layout,
    assetImages as List<Any>
) {
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var row = convertView
        val inflater = context.layoutInflater
        if (convertView == null) row =
            inflater.inflate(R.layout.new_assets_button_layout, null, true)

        val assetNameTextView: Button? = row?.findViewById(R.id.new_AssetListButton)
        val assetImage: ImageView? = row?.findViewById(R.id.new_AssetListImage)

        assetNameTextView?.text = assetName[position]

        return row!!
    }


}
