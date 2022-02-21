package com.jawaadianinc.valorant_stats

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
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
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.brawlhalla.brawlFindAccount
import com.jawaadianinc.valorant_stats.valorant.FindAccount
import com.squareup.picasso.Picasso


class GamePickerMenu : AppCompatActivity() {
    private val RC_SIGN_IN: Int = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_picker)

        val toolbar: Toolbar = findViewById<View>(R.id.toolbar2) as Toolbar
        setSupportActionBar(toolbar)

        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

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

        valoButton.animate().alpha(1f).translationYBy(200f).duration = 600
        brawlButton.animate().alpha(1f).translationYBy(200f).setDuration(600).startDelay = 200
        fortniteButton.animate().alpha(1f).translationYBy(200f).setDuration(600).startDelay = 400
        apexButton.animate().alpha(1f).translationYBy(200f).setDuration(600).startDelay = 600

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

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()
        val mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/players")
        val brawlRef = database.getReference("Brawlhalla/players")
        //val gameReuqestRef = database.getReference("gameRequests")

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

        val account = GoogleSignIn.getLastSignedInAccount(this)
        val signIn: SignInButton = findViewById(R.id.sign_in_button)
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
        val signIn: SignInButton = findViewById(R.id.sign_in_button)
        val nameText: TextView = findViewById(R.id.accountName)
        val pic: ImageView = findViewById(R.id.accountProfile)
        val circle = findViewById<View>(R.id.circle)
        signIn.visibility = View.INVISIBLE
        nameText.visibility = View.VISIBLE
        pic.visibility = View.VISIBLE
        circle.visibility = View.VISIBLE
        nameText.text = "Welcome ${name}!"
        Picasso.get().load(profilePic).fit().centerInside()
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

    fun goToSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    fun goToAbout() {
        //TODO add about section to app!
    }

}
