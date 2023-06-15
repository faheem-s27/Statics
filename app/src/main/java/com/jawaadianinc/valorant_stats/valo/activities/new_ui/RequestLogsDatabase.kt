package com.jawaadianinc.valorant_stats.valo.activities.new_ui

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RequestLogsDatabase(context: Context) : SQLiteOpenHelper(context, "requestLogs.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $RequestLogsDatabase ($LogID INTEGER PRIMARY KEY AUTOINCREMENT, $URL TEXT, $methodType TEXT, $dateTime TEXT, $code INTEGER, $body TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun addLog(url: String, method: String, timeDate: String, codeLog: Int, bodyLog: String): Boolean {
        val database: SQLiteDatabase = this.writableDatabase
        val contentValues = ContentValues().apply {
            put(URL, url)
            put(methodType, method)
            put(dateTime, timeDate)
            put(code, codeLog)
            put(body, bodyLog)
        }
        val success = database.insert(RequestLogsDatabase, null, contentValues) != -1L
        contentValues.clear()
        return success
    }

    fun deleteLog(logID: String): Boolean {
        val database: SQLiteDatabase = this.writableDatabase
        return database.delete(RequestLogsDatabase, "$LogID = ?", arrayOf(logID)) != -1
    }

    fun getLogs(): ArrayList<RequestLog>
    {
        val logs = ArrayList<RequestLog>()
        val database: SQLiteDatabase = this.readableDatabase
        val cursor = database.rawQuery("SELECT * FROM $RequestLogsDatabase", null)
        if (cursor.moveToFirst()) {
            do {
                val url = cursor.getString(1)
                val method = cursor.getString(2)
                val dateTime = cursor.getString(3)
                val code = cursor.getInt(4)
                val body = cursor.getString(5)
                logs.add(RequestLog(url, method, dateTime, code, body))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return logs
    }

    companion object {
        const val LogID = "LogID"
        const val URL = "URL"
        const val methodType = "methodType"
        const val dateTime = "dateTime"
        const val code = "code"
        const val body = "body"
        const val RequestLogsDatabase = "RequestLogsDatabase"
    }
}
