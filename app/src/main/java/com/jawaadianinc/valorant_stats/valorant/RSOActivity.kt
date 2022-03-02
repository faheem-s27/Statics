package com.jawaadianinc.valorant_stats.valorant

import android.net.Uri
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.jawaadianinc.valorant_stats.R


class RSOActivity : AppCompatActivity() {
    var code: String? = null
    var secret: String? = null
    var base64encode: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rsoactivity)
        val data: Uri? = intent?.data
        val updateText: TextView = findViewById(R.id.infoText)
        code = data!!.getQueryParameter("code")

        updateText.text = "Accessing information"

        val database = Firebase.database.getReference("VALORANT/SuperSecret")
        database.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                secret = dataSnapshot.value as String?
                val toBeEncoded = "statics:$secret"
                base64encode = Base64.encodeToString(toBeEncoded.toByteArray(), Base64.DEFAULT)
                updateText.text = "Collecting information"
                authenticate()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })


    }

    fun authenticate() {
        Log.d(
            "RSO",
            "INFORMATION: Code = $code\nBefore encoding = statics:$secret\nEncoded = $base64encode"
        )

    }
}
