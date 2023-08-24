package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.Context
import android.util.AttributeSet
import android.widget.ImageView
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.jawaadianinc.valorant_stats.R

class ProfilePicturePreference(context: Context, attrs: AttributeSet?) :
    Preference(context, attrs) {

    init {
        layoutResource = R.layout.preference_screen_with_picture // Replace with the layout filename
    }

    override fun onBindViewHolder(holder: PreferenceViewHolder) {
        super.onBindViewHolder(holder)
        // Initialize the ImageView and set the profile picture
        val profilePicture = holder.itemView.findViewById<ImageView>(R.id.profilePicture)
        profilePicture.setImageResource(R.drawable.just_statics_alot_smaller) // Replace with the actual resource
    }
}
