package com.jawaadianinc.valorant_stats

import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.FirebaseApp
import com.google.firebase.appcheck.FirebaseAppCheck
import com.google.firebase.appcheck.safetynet.SafetyNetAppCheckProviderFactory
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class LoadingActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val loadingProgressBar: ProgressBar = findViewById(R.id.progressBar4)
        val loadingText: TextView = findViewById(R.id.textView4)
        loadingProgressBar.alpha = 0.2f
        loadingText.alpha = 0.2f

        loadingProgressBar.translationY = +100f
        loadingText.translationY = +100f

        loadingProgressBar.animate().alpha(1f).translationYBy(-100f).duration = 500
        loadingText.animate().alpha(1f).translationYBy(-100f).duration = 500


        if (!isNetworkAvailable()) {
            loadingProgressBar.visibility = View.INVISIBLE
            loadingText.text = "Error!\nStatics requires an active internet connection!"
        }

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                Toast.makeText(
                    this,
                    "There's an update available on the Play Store!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

        FirebaseApp.initializeApp(/*context=*/this)
        val firebaseAppCheck = FirebaseAppCheck.getInstance()
        firebaseAppCheck.installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/players")
        playersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val number = dataSnapshot.childrenCount
                startActivity(Intent(this@LoadingActivity, GamePickerMenu::class.java))
                overridePendingTransition(R.anim.fadein, R.anim.fadeout)
                finish()
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@LoadingActivity,
                    "Failed to connect to Statics!",
                    Toast.LENGTH_SHORT
                ).show()
                loadingProgressBar.visibility = View.GONE
                loadingText.text = "An error occured while connecting to Statics :("
            }
        })
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }
}




