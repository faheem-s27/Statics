package com.jawaadianinc.valorant_stats.valorant

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class StatsAdaptor(fm: FragmentManager?, var totalTabs: Int) :

    FragmentPagerAdapter(fm!!) {

    override fun getItem(position: Int): Fragment {
        return when (position) {
            0 -> {
                CompetitiveStats()
            }
            1 ->{
                UnratedStats()
            }
            2-> {
                SpikeRushStats()
            }

            3-> {
                Deathmatch()
            }


            else -> throw IllegalStateException("position $position is invalid for this viewpager")
        }
    }

    // this counts total number of tabs
    override fun getCount(): Int {
        return totalTabs
    }
}