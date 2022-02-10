package com.jawaadianinc.valorant_stats.brawlhalla

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class BrawlAdapter(fm: FragmentManager?, var totalTabs: Int) :

    FragmentPagerAdapter(fm!!) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                brawl_playerStats()
            }
            1 -> {
                brawl_playerRanked()
            }
            2 -> {
                brawl_playerRanked2v2s()
            }
            3 -> {
                brawl_playerClan()
            }
            else -> throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}
