package com.jawaadianinc.valorant_stats.valorant

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jawaadianinc.valorant_stats.R

class RSOActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rsoactivity)

        val oAuth = intent.extras?.get("OAuth")
        if (oAuth == "Success!") {
            Toast.makeText(this, "Login Success!", Toast.LENGTH_SHORT).show()
        }
    }
}
