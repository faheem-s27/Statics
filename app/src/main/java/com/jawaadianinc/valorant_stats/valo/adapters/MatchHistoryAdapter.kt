package com.jawaadianinc.valorant_stats.valo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.jawaadianinc.valorant_stats.valo.match_info.*

class MatchHistoryAdapter(fm: FragmentManager?, private var totalTabs: Int) :

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
