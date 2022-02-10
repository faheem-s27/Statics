package com.jawaadianinc.valorant_stats

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.brawlhalla.brawlFindAccount
import com.jawaadianinc.valorant_stats.valorant.FindAccount


class GamePickerMenu : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_picker)

        val toolbar: Toolbar = findViewById<View>(R.id.toolbar2) as Toolbar
        setSupportActionBar(toolbar)

        val textStats: TextView = findViewById(R.id.databaseStatsValo)
        val brawlStats: TextView = findViewById(R.id.databaseStatsBrawl)
        val valoButton: Button = findViewById(R.id.valo)
        val brawlButton: Button = findViewById(R.id.brawl)
        val apexButton: Button = findViewById(R.id.apex)
        val fortniteButton: Button = findViewById(R.id.fortnite)
        val requestButton: Button = findViewById(R.id.request)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        valoButton.alpha = 0f
        brawlButton.alpha = 0f
        apexButton.alpha = 0f
        fortniteButton.alpha = 0f

        valoButton.translationY = -200f
        brawlButton.translationY = -200f
        apexButton.translationY = -200f
        fortniteButton.translationY = -200f


        valoButton.animate().alpha(1f).translationYBy(200f).duration = 1500
        brawlButton.animate().alpha(1f).translationYBy(200f).setDuration(1500).startDelay = 400
        fortniteButton.animate().alpha(1f).translationYBy(200f).setDuration(1500).startDelay = 800
        apexButton.animate().alpha(1f).translationYBy(200f).setDuration(1500).startDelay = 1200

        valoButton.setOnClickListener {
            startActivity(Intent(this, FindAccount::class.java))
        }

        brawlButton.setOnClickListener {
            startActivity(Intent(this, brawlFindAccount::class.java))
        }

        apexButton.setOnClickListener {
            Toast.makeText(this, "Coming soon", Toast.LENGTH_SHORT).show()
        }

        fortniteButton.setOnClickListener {
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
    }

    private fun showAlertWithTextInputLayout(context: Context) {
        val textInputLayout = TextInputLayout(context)
        val input = EditText(context)
        textInputLayout.hint = "Game name"
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_valorant, menu)
        return menu.let { super.onCreateOptionsMenu(it) }
    }

    override fun onOptionsItemSelected(@NonNull item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> {
                goToSettings()
            }
            R.id.About -> {}
        }
        return super.onOptionsItemSelected(item)
    }

    fun goToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun goToAbout() {
        //TODO add about section to app!
    }
}
