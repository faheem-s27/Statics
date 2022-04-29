package com.jawaadianinc.valorant_stats.valo

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class MatchHistoryAdapter(fm: FragmentManager?, var totalTabs: Int) :

    FragmentPagerAdapter(fm!!) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                MatchDetailsFragment()
            }
            1 -> {
                PlayerDetailsFragment()
            }
            2 -> {
                RoundsDetailsFragment()
            }
            3 -> {
                RoundsMoreDetailsFragment()
            }
            4 -> {
                kill_map_Fragment()
            }
            5 -> {
                kill_feed_Fragment()
            }
            else -> throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}
