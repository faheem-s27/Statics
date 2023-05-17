package com.jawaadianinc.valorant_stats.valo.activities

import android.app.AlertDialog
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.jawaadianinc.valorant_stats.LastMatchWidget
import com.jawaadianinc.valorant_stats.R
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.BlurTransformation
import org.jetbrains.anko.doAsync
import org.json.JSONArray
import org.json.JSONObject
import java.net.URL

class LoggingInActivityRSO : AppCompatActivity() {
    private lateinit var imagebackground: ImageView
    private val imagesURL = java.util.ArrayList<String>()
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logging_in_rso)
        val hyperLink: TextView = findViewById(R.id.textView11)
        hyperLink.movementMethod = LinkMovementMethod.getInstance()

        val widgetIntent = Intent(this, LastMatchWidget::class.java)
        widgetIntent.action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
        val ids = AppWidgetManager.getInstance(application).getAppWidgetIds(
            ComponentName(applicationContext, LastMatchWidget::class.java)
        )
        widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, ids)
        sendBroadcast(widgetIntent)

        imagesURL.add("https://media.valorant-api.com/playercards/3432dc3d-47da-4675-67ae-53adb1fdad5e/largeart.png")
        doAsync {
            val getValoImagesURL =
                JSONObject(URL("https://valorant-api.com/v1/playercards").readText())
            val images = getValoImagesURL["data"] as JSONArray
            for (i in 0 until images.length()) {
                val imageURL = images[i] as JSONObject
                imagesURL.add(imageURL["largeArt"].toString())
            }
        }

        imagebackground = findViewById(R.id.imageView4)
        Picasso.get().load(imagesURL.random())
            .transform(BlurTransformation(this)).fit().centerInside()
            .into(imagebackground)
        handler = Handler(Looper.getMainLooper())
        runnable = Runnable {
            doTask(handler)
        }
        handler.postDelayed(runnable, 3000)

        val signIn = findViewById<Button>(R.id.signIn)
        val checkBox = findViewById<CheckBox>(R.id.checkBox)

        // check if the checkbox is checked doing on check change listener
        checkBox.setOnCheckedChangeListener { _, isChecked ->
            // dim the button if the checkbox is not checked
            if (isChecked) {
                signIn.alpha = 1f
            } else {
                signIn.alpha = 0.5f
            }
        }

        // if checkbox is not checked, dim the button
        if (!checkBox.isChecked) {
            signIn.alpha = 0.5f
        }

        signIn.setOnClickListener {
            // check if checkbox is checked
            if (checkBox.isChecked) {
                startActivity(
                    Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://auth.riotgames.com/authorize?client_id=statics&redirect_uri=https://statics-fd699.web.app/authorize.html&response_type=code&scope=openid+offline_access&prompt=login")
                    )
                )
                finish()
            } else {
                val builder = AlertDialog.Builder(this)
                builder.setTitle("Error")
                builder.setMessage(getString(R.string.acceptTermsConditions))
                builder.setPositiveButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                val dialog: AlertDialog = builder.create()
                dialog.show()
            }
        }

        val msg = getString(R.string.s7)

        val howitworks: FloatingActionButton = findViewById(R.id.howitworksBT)
        howitworks.setOnClickListener {
            // show an alert dialog that explains the process of RSO taking in the token and how it does not store any passwords
            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle(getString(R.string.s8))
            alertDialog.setMessage(msg)
            alertDialog.setPositiveButton("OK") { dialog, which ->
                dialog.dismiss()
            }
            alertDialog.show()
        }


    }

    private fun doTask(handler: Handler) {
        Picasso.get().load(imagesURL.random()).placeholder(imagebackground.drawable)
            .transform(BlurTransformation(this)).fit().centerInside()
            .into(imagebackground)
        handler.postDelayed(runnable, 3000)
    }

}
