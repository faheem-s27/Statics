package com.jawaadianinc.valorant_stats

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.preference.PreferenceManager
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jawaadianinc.valorant_stats.main.SplashActivity
import com.jawaadianinc.valorant_stats.main.ValorantAccountDatabase
import com.squareup.picasso.Picasso
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyFirebaseMessagingService : FirebaseMessagingService() {
    @OptIn(DelicateCoroutinesApi::class)
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d("FirebaseMessage", "Message received")

        val accounts = ValorantAccountDatabase(this).getAllValorantAccounts()
        val title = remoteMessage.data["title"]
        val body = remoteMessage.data["body"]
        val icon = remoteMessage.data["icon"]

        val accountName = title!!.split(" ")
        val account = accounts.find { it.name == accountName[accountName.size - 1] }
        val imageID = accounts[0].getImage()

        if (account != null) {
            return
        }

        val intent = Intent(this@MyFirebaseMessagingService, SplashActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this@MyFirebaseMessagingService,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE
        )


        // check if the prefernece is enabled
        val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val chat = sharedPreferences.getBoolean("chat_notifications", true)
        if (chat) {
            GlobalScope.launch {
                val bitmap: Bitmap? = withContext(Dispatchers.IO) {
                    Picasso.get().load(imageID).get()
                }

                // Create and display a notification
                val notificationBuilder =
                    NotificationCompat.Builder(this@MyFirebaseMessagingService, "chat_notification")
                        .setContentTitle(title)
                        .setContentText(body)
                        .setAutoCancel(true)
                        .setSmallIcon(R.drawable.statics_mono)
                        .setStyle(
                            NotificationCompat.BigTextStyle()
                                .bigText(body)
                        )
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setContentIntent(pendingIntent)
                        .setCategory(NotificationCompat.CATEGORY_MESSAGE)

                if (bitmap != null) {
                    notificationBuilder.setLargeIcon(bitmap)
                }

                val notificationManager =
                    getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                notificationManager.notify(0, notificationBuilder.build())
            }
        }
    }

    override fun onNewToken(token: String) {

        sendRegistrationToServer(token)
        // Upload the new FCM token to the database
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putString("fcm_token", token)
        editor.apply()
    }

    private fun sendRegistrationToServer(token: String) {
        val database = Firebase.database
        val myRef = database.getReference("Statics/FCM_Tokens")
        myRef.setValue(token)
    }
}
