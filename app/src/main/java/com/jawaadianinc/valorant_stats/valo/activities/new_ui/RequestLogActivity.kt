package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ListView
import com.jawaadianinc.valorant_stats.R
import com.jawaadianinc.valorant_stats.valo.adapters.RequestLogAdapter

class RequestLogActivity : AppCompatActivity() {
    private lateinit var database : RequestLogsDatabase
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_request_log)

        database = RequestLogsDatabase(this)
        val logs = database.getLogs()
        // sort logs by date
        logs.sortByDescending { it.dateTime }
        val logAdapter = RequestLogAdapter(this, logs)
        val listView : ListView = findViewById(R.id.listViewLogs)
        listView.adapter = logAdapter


    }
}