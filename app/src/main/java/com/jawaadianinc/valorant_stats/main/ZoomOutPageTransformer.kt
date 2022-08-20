package com.jawaadianinc.valorant_stats.main

import android.view.View
import androidx.viewpager.widget.ViewPager

private const val MIN_SCALE = 0.85f
private const val MIN_ALPHA = 0.5f

class ZoomOutPageTransformer : ViewPager.PageTransformer {

    override fun transformPage(view: View, position: Float) {
        if (position <= -1.0f || position >= 1.0f) {
            view.translationX = view.width * position
            view.alpha = 0.0f
        } else if (position == 0.0f) {
            view.translationX = view.width * position
            view.alpha = 1.0f
        } else {
            // position is between -1.0F & 0.0F OR 0.0F & 1.0F
            view.translationX = view.width * -position
            view.alpha = 1.0f - Math.abs(position)
        }
    }
}
