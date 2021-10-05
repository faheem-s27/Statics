package com.jawaadianinc.valorant_stats

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity


class CompareActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compare)


        val i = Intent(this, FindAccount::class.java)
            .setFlags(
                Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT or
                        Intent.FLAG_ACTIVITY_NEW_TASK or
                        Intent.FLAG_ACTIVITY_MULTIPLE_TASK
            )


        if (isInMultiWindowMode){
            startActivity(i)
        }

        else{
            AlertDialog.Builder(this).setTitle("SplitScreen Error!")
                .setMessage("Use split screen mode first before comparing stats")
                .setPositiveButton(android.R.string.ok) { _, _ ->
                    Toast.makeText(applicationContext,
                        "Well done", Toast.LENGTH_SHORT).show()
                }
                .setIcon(android.R.drawable.ic_dialog_alert).show()
        }

        val magicButton: Button = findViewById(R.id.magic)

        magicButton.setOnClickListener {
            startActivity(Intent(this, FindAccount::class.java))
        }
    }

}