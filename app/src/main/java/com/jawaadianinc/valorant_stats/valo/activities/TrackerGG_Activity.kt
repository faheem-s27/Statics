package com.jawaadianinc.valorant_stats.valo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.databinding.ActivityTrackerGgBinding

class TrackerGG_Activity : AppCompatActivity() {

    private lateinit var binding: ActivityTrackerGgBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityTrackerGgBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navController = findNavController(R.id.nav_host_fragment_activity_tracker_gg)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_maps, R.id.navigation_agents, R.id.navigation_weapons
            )
        )
        //setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_activity_tracker_gg)
        return navController.navigateUp() || super.onSupportNavigateUp()
    }

}
