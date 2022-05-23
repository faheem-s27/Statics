package com.jawaadianinc.valorant_stats

import android.annotation.SuppressLint
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
import com.jawaadianinc.valorant_stats.valo.LoggingInActivityRSO
import com.jawaadianinc.valorant_stats.valo.PlayerDatabase
import com.jawaadianinc.valorant_stats.valo.ValorantMainMenu


class LoadingActivity : AppCompatActivity() {
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_loading)

        val loadingProgressBar: ProgressBar = findViewById(R.id.progressBar4)
        val updateText: TextView = findViewById(R.id.textView4)

        loadingProgressBar.alpha = 0.2f
        updateText.alpha = 0.2f

        loadingProgressBar.translationY = +100f
        updateText.translationY = +100f

        loadingProgressBar.animate().alpha(1f).translationYBy(-100f).duration = 500
        updateText.animate().alpha(1f).translationYBy(-100f).duration = 500

        if (!isNetworkAvailable()) {
            loadingProgressBar.visibility = View.INVISIBLE
            updateText.text = "Error!\nStatics requires an active internet connection!"
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
        FirebaseAppCheck.getInstance().installAppCheckProviderFactory(
            SafetyNetAppCheckProviderFactory.getInstance()
        )

        updateText.text = "Loading resources"
        val database = Firebase.database
        val playersRef = database.getReference("VALORANT/key")


        playersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val key = (dataSnapshot.value as String?).toString()
                updateText.text = "Starting"
                val valoName = PlayerDatabase(this@LoadingActivity).getPlayerName()
                valoAccountStats(valoName, key)
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@LoadingActivity,
                    "Failed to connect to Statics!",
                    Toast.LENGTH_SHORT
                ).show()
                loadingProgressBar.visibility = View.GONE
                updateText.text = "An error occurred while connecting to Statics :("
            }
        })
    }

    private fun isNetworkAvailable(): Boolean {
        val cm = getSystemService(
            Context.CONNECTIVITY_SERVICE
        ) as ConnectivityManager
        return cm.activeNetworkInfo?.isConnected == true
    }

    private fun valoAccountStats(valoName: String?, key: String) {
        if (valoName == null) {
            val intent = Intent(this, LoggingInActivityRSO::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            finish()
        } else {
            val intent = Intent(this, ValorantMainMenu::class.java)
            intent.putExtra("key", key)
            startActivity(intent)
            overridePendingTransition(R.anim.fadein, R.anim.fadeout)
            finish()
        }
    }

}




