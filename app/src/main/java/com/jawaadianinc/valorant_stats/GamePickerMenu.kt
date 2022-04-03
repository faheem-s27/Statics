package com.jawaadianinc.valorant_stats

import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.NonNull
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.brawlhalla.brawlFindAccount
import com.jawaadianinc.valorant_stats.valorant.CosmeticsActivity
import com.jawaadianinc.valorant_stats.valorant.LoggingInActivityRSO
import com.jawaadianinc.valorant_stats.valorant.PlayerDatabase
import com.jawaadianinc.valorant_stats.valorant.PlayerMainMenu
import com.squareup.picasso.Picasso


class GamePickerMenu : AppCompatActivity() {
    private val RC_SIGN_IN: Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_picker)

        val toolbar: Toolbar = findViewById<View>(R.id.toolbar2) as Toolbar
        setSupportActionBar(toolbar)

        val textStats: TextView = findViewById(R.id.databaseStatsValo)
        textStats.gravity = Gravity.CENTER
        val brawlStats: TextView = findViewById(R.id.databaseStatsBrawl)
        val valoButton: ImageButton = findViewById(R.id.valo)
        val brawlButton: Button = findViewById(R.id.brawl)
        val apexButton: Button = findViewById(R.id.apex)
        val requestButton: Button = findViewById(R.id.request)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        val scroll: View = findViewById(R.id.scroll)
        //scroll.alpha = 0f
        //scroll.translationY = 0f

        valoButton.alpha = 0f
        brawlButton.alpha = 0f
        apexButton.alpha = 0f

        valoButton.translationY = -200f
        brawlButton.translationY = -200f
        apexButton.translationY = -200f

        valoButton.animate().alpha(1f).translationYBy(200f).duration = 600
        brawlButton.animate().alpha(1f).translationYBy(200f).setDuration(600).startDelay = 200
        apexButton.animate().alpha(0.3f).translationYBy(200f).setDuration(600).startDelay = 400

        val videoView = findViewById<VideoView>(R.id.videoView)
        videoView.setVideoPath("https://firebasestorage.googleapis.com/v0/b/statics-fd699.appspot.com/o/splashLoading_Trim.mp4?alt=media&token=a59cf7fe-b77b-4109-9e25-c3dd68804d9c")
        videoView.setOnPreparedListener(MediaPlayer.OnPreparedListener {
            // when vid is done preparing
            it.setVolume(0f, 0f)
            videoView.start()
        })

        val valoName = PlayerDatabase(this).getPlayerName()
        valoButton.setOnClickListener {
            valoAccountStats(valoName)
//            val builder = android.app.AlertDialog.Builder(this)
//            builder.setTitle("Select an option")
//            builder.setItems(
//                arrayOf<CharSequence>("Account Stats", "In-Game Cosmetics"))
//            { _, which ->
//                when (which) {
//                    0 -> valoAccountStats(valoName)
//                    1 -> valoCosmetics()
//                }}
//            val dialog = builder.create()
//            dialog.window!!.attributes.windowAnimations =
//                R.style.DialogAnimation_2
//            dialog.show()
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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso)

        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/RSO")
        val brawlRef = database.getReference("Brawlhalla/players")
        //val gameReuqestRef = database.getReference("gameRequests")

        playersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val number = dataSnapshot.childrenCount
                if (valoName == null) {
                    textStats.text = "Not signed in\nTracking $number VALORANT players!"
                } else {
                    textStats.text = "Signed in as $valoName\nTracking $number VALORANT players!"
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
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

        val account = GoogleSignIn.getLastSignedInAccount(this)
        val signIn: Button = findViewById(R.id.signInGoogle)
        val name: TextView = findViewById(R.id.accountName)
        val pic: ImageView = findViewById(R.id.accountProfile)
        val circle = findViewById<View>(R.id.circle)
        val logOut: Button = findViewById(R.id.logOut)

        logOut.setOnClickListener {
            mGoogleSignInClient.signOut()
                .addOnCompleteListener(this) {
                    startActivity(Intent(this, SplashActivity::class.java))
                }
        }

        if (account == null) {
            signIn.visibility = View.VISIBLE
            name.visibility = View.INVISIBLE
            pic.visibility = View.INVISIBLE
            circle.visibility = View.INVISIBLE
        } else {
            showProfile(account.displayName!!, account.photoUrl!!)
        }

        signIn.setOnClickListener {
            val signInIntent: Intent = mGoogleSignInClient.signInIntent
            startActivityForResult(signInIntent, RC_SIGN_IN)
        }
    }

    private fun valoCosmetics() {
        startActivity(Intent(this, CosmeticsActivity::class.java))
    }

    private fun valoAccountStats(valoName: String?) {
        if (valoName == null) {
            startActivity(Intent(this, LoggingInActivityRSO::class.java))
        } else {
            startActivity(Intent(this, PlayerMainMenu::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            showProfile(account.displayName!!, account.photoUrl!!)
        } catch (e: ApiException) {
            Toast.makeText(this, "Failed to sign in :/", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showProfile(name: String, profilePic: Uri) {
        val account = GoogleSignIn.getLastSignedInAccount(this)

        val signIn: Button = findViewById(R.id.signInGoogle)
        val nameText: TextView = findViewById(R.id.accountName)
        val pic: ImageView = findViewById(R.id.accountProfile)
        val circle = findViewById<View>(R.id.circle)
        signIn.visibility = View.INVISIBLE
        nameText.visibility = View.VISIBLE
        pic.visibility = View.VISIBLE
        circle.visibility = View.VISIBLE
        nameText.text = "Hi ${name}!"
        Picasso.get().load(account?.photoUrl).fit().centerInside()
            .into(pic)
        val database = Firebase.database
        val accounts = database.getReference("Users/Google")
        accounts.child(name).child("PhotoURL").setValue(profilePic.toString())

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

    private fun goToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun goToAbout() {
        //TODO add about section to app!
    }

}
