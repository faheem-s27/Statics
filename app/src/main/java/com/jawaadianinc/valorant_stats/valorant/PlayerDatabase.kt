package com.jawaadianinc.valorant_stats.valorant

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PlayerDatabase(context: Context) : SQLiteOpenHelper(context, "valoPlayers.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $VALOPLAYERS (ID INTEGER PRIMARY KEY AUTOINCREMENT, $PUUID TEXT, $USERNAME TEXT, $USERTAG TEXT, $PRIVACY TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun isPlayerSignedIn(): String? {
        var playerName: String? = null
        val sqlString = "SELECT * FROM $VALOPLAYERS"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(sqlString, null)
        if (cursor.moveToFirst()) {
            playerName = cursor.getString(2) + "#" + cursor.getString(3)
        }
        return playerName
    }

    fun removePlayer() {}

    fun playerList() {}


    companion object {
        const val PUUID = "PUUID"
        const val USERNAME = "USERNAME"
        const val USERTAG = "USERTAG"
        const val PRIVACY = "PRIVACY"
        const val VALOPLAYERS = "VALOPLAYERS"
    }

    fun addPlayer() {
        //TODO add method for adding players to database after RSO authentication
    }

}
