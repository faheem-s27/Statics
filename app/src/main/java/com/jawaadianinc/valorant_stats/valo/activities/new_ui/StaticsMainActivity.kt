package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityStaticsMainBinding

class StaticsMainActivity : AppCompatActivity() {
    lateinit var playerName: String
    lateinit var region: String
    lateinit var key: String

    private lateinit var binding: ActivityStaticsMainBinding
    lateinit var toolbar: MaterialToolbar
    private lateinit var bottomNavBar: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStaticsMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        toolbar = binding.newMainMenuToolBar2
        bottomNavBar = binding.bottomNavigationView

        setSupportActionBar(toolbar)

        // get the player name from the previous activity
        playerName = intent.getStringExtra("playerName").toString()
        region = intent.getStringExtra("region").toString()
        key = intent.getStringExtra("key").toString()

        val statsFragment = StaticsMainMenu()
        val LiveStatsFragment = LiveStatsFragment()
        val AssetsFragment = AssetsFragment()
        
        setCurrentFragment(statsFragment)

        bottomNavBar.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.new_Stats -> {
                    statsFragment.ISACTIVE = true
                    setCurrentFragment(statsFragment)
                    true
                }
                R.id.new_Live -> {
                    statsFragment.ISACTIVE = false
                    setCurrentFragment(LiveStatsFragment)
                    true
                }
                R.id.new_Assets -> {
                    statsFragment.ISACTIVE = false
                    setCurrentFragment(AssetsFragment)
                    true
                }
                else -> {
                    false
                }
            }
        }


    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment)
            commit()
        }
}
