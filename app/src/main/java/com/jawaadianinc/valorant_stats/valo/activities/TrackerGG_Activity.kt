package com.jawaadianinc.valorant_stats.valo.activities

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
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
        navView.setupWithNavController(navController)

        val mode = intent.getStringExtra("mode")
        val playerName = intent.getStringExtra("playerName")
        val toolbar = binding.toolbar4
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.title = "${playerName!!.split("#")[0]}'s $mode stats"
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }
}
