package com.jawaadianinc.valorant_stats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

class GamePicker : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_picker)
        val textStats: TextView = findViewById(R.id.databaseStatsValo)
        val brawlStats: TextView = findViewById(R.id.databaseStatsBrawl)
        val valoButton: Button = findViewById(R.id.valo)
        val brawlButton: Button = findViewById(R.id.brawl)
        val apexButton: Button = findViewById(R.id.apex)
        val requestButton: Button = findViewById(R.id.request)
        val requestStats: TextView = findViewById(R.id.databaseGameRequests)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        valoButton.setOnClickListener {
            startActivity(Intent(this, FindAccount::class.java))
        }

        brawlButton.setOnClickListener {
            startActivity(Intent(this, brawlFindAccount::class.java))
        }

        apexButton.setOnClickListener {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
        }

        requestButton.setOnClickListener {
            showAlertWithTextInputLayout(this)
        }

        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/players")
        val brawlRef = database.getReference("Brawlhalla/players")
        val gameReuqestRef = database.getReference("gameRequests")

        playersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val number = dataSnapshot.childrenCount
                textStats.text = "Tracking $number VALORANT players!"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

        brawlRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val number = dataSnapshot.childrenCount
                brawlStats.text = "Tracking $number Brawlhalla players!"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })

        gameReuqestRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val number = dataSnapshot.childrenCount
                requestStats.text = "$number game requests"
            }

            override fun onCancelled(databaseError: DatabaseError) {
                println("The read failed: " + databaseError.code)
            }
        })
    }

    private fun showAlertWithTextInputLayout(context: Context) {
        val textInputLayout = TextInputLayout(context)
        val input = EditText(context)
        textInputLayout.hint = "Name of game"
        textInputLayout.addView(input)
        textInputLayout.setPadding(
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            0,
            resources.getDimensionPixelOffset(R.dimen.dp_19),
            0
        )
        val database = Firebase.database
        val gameReuqestRef = database.getReference("gameRequests")
        val alert = AlertDialog.Builder(context)
            .setTitle("Request a game")
            .setView(textInputLayout)
            .setMessage("Please enter a game that you want to see stats on")
            .setPositiveButton("Submit") { dialog, _ ->
                // do some thing with input.text
                if (input.text.isNotEmpty()) {
                    gameReuqestRef.push().setValue(input.text.toString())
                    val contextView = findViewById<View>(R.id.request)
                    val snackbar = Snackbar
                        .make(contextView, "Sent request!", Snackbar.LENGTH_LONG)
                    snackbar.show()
                    dialog.cancel()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }.create()
        alert.show()
    }
}
