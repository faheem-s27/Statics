package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation

class ESportsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_e_sports, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bg = view.findViewById<ImageView>(R.id.imageView8)
        Picasso.get().load(StaticsMainActivity.playerCardLarge).fit().centerCrop()
            .transform(BlurTransformation(requireContext())).into(bg)
    }

}
