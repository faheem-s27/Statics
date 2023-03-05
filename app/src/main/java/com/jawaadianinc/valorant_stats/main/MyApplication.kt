package com.jawaadianinc.valorant_stats.main

import android.app.Application
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.color.DynamicColors
import com.google.firebase.database.FirebaseDatabase


class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        // This is all you need.
        DynamicColors.applyToActivitiesIfAvailable(this)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        FirebaseDatabase.getInstance().setPersistenceEnabled(true)
    }
}
