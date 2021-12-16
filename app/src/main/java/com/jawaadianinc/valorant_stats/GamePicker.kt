package com.jawaadianinc.valorant_stats

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GamePicker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_picker)
        val textStats: TextView = findViewById(R.id.databaseStats)
        val valoButton: Button = findViewById(R.id.valo)
        val brawlButton: Button = findViewById(R.id.brawl)
        val apexButton: Button = findViewById(R.id.apex)

        valoButton.setOnClickListener {
            startActivity(Intent(this, FindAccount::class.java))
        }

        brawlButton.setOnClickListener {
            startActivity(Intent(this, brawlFindAccount::class.java))
        }

        apexButton.setOnClickListener {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
        }

        val postListener = object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // Get Post object and use the values to update the UI
                val number = dataSnapshot.childrenCount
                textStats.text = "Tracking $number VALORANT players!"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting Post failed, log a message
                Log.w(TAG, "loadPost:onCancelled", databaseError.toException())
            }
        }

        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/players")
        playersRef.addListenerForSingleValueEvent(postListener)

    }
}