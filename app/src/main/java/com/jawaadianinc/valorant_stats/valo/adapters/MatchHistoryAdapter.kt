package com.jawaadianinc.valorant_stats.valo.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import com.jawaadianinc.valorant_stats.valo.match_info.*

class MatchHistoryAdapter(fm: FragmentManager?, private var totalTabs: Int) :
    FragmentPagerAdapter(fm!!, FragmentPagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                RoundOverview()
            }
            1 -> {
                MatchDetailsFragment()
            }
            2 -> {
                PlayerDetailsFragment()
            }
            3 -> {
                RoundsDetailsFragment()
            }
            4 -> {
                RoundsMoreDetailsFragment()
            }
            5 -> {
                KillMapFragment()
            }
            6 -> {
                KillFeedFragment()
            }

            else -> throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}
