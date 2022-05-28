package com.jawaadianinc.valorant_stats.valo.cosmetics.weapon

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class WeaponAdapter(fm: FragmentManager?, private var totalTabs: Int) :

    FragmentPagerAdapter(fm!!) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                WeaponStats()
            }
            1 -> {
                WeaponVideos()
            }
            2 -> {
                WeaponSkins()
            }
            else -> throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}
