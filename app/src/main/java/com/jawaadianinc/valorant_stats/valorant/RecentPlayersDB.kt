package com.jawaadianinc.valorant_stats.valorant

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class RecentPlayersDB(context: Context) : SQLiteOpenHelper(context, "playersFromLink.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $RECENTPLAYERS ($PUUID TEXT PRIMARY KEY, $USERNAME TEXT, $USERTAG TEXT, $REGION TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        TODO("Not yet implemented")
    }

    fun addRecentPlayer(gameName: String, gameTag: String, puuid: String, region: String): Boolean {
        val dbRead = readableDatabase
        val sqlString = "SELECT * FROM $RECENTPLAYERS WHERE $PUUID = '$puuid'"
        val cursor = dbRead.rawQuery(sqlString, null)
        if (!cursor.moveToFirst()) {
            //method to add a player to the database received from the link
            val db = this.writableDatabase
            val cv = ContentValues()
            cv.put(PUUID, puuid)
            cv.put(USERNAME, gameName)
            cv.put(USERTAG, gameTag)
            cv.put(REGION, region)
            val insert = db.insert(RECENTPLAYERS, null, cv)
            db.close()
            return insert != -1L
        }

        return true
    }


    companion object {
        const val PUUID = "PUUID"
        const val USERNAME = "USERNAME"
        const val USERTAG = "USERTAG"
        const val RECENTPLAYERS = "RECENTPLAYERS"
        const val REGION = "REGION"
    }

}
