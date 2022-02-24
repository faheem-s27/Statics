package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.dynamiclinks.ktx.dynamicLinks
import com.google.firebase.ktx.Firebase
import kotlin.system.exitProcess

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_YES)
        startActivity(Intent(this, GamePickerMenu::class.java))
        finish()

        Firebase.dynamicLinks
            .getDynamicLink(intent)
            .addOnSuccessListener(this) { pendingDynamicLinkData ->
                // Get deep link from result (may be null if no link is found)
                var deepLink: Uri? = null
                if (pendingDynamicLinkData != null) {
                    deepLink = pendingDynamicLinkData.link
                    Toast.makeText(this, deepLink.toString(), Toast.LENGTH_SHORT).show()
//                    val intent = Intent(this, RSOActivity::class.java)
//                    intent.putExtra("OAuth", "Success!")
//                    startActivity(intent)
                }
            }
            .addOnFailureListener(this) {
                Toast.makeText(
                    this,
                    "This device doesn't meet the security requirements",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
                exitProcess(0)
            }

        val appUpdateManager = AppUpdateManagerFactory.create(this)
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        appUpdateInfoTask.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)
            ) {
                Toast.makeText(
                    this,
                    "There's an update available on the play store!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
