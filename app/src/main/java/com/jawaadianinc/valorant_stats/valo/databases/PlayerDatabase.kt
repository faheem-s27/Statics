package com.jawaadianinc.valorant_stats.valo.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class PlayerDatabase(context: Context) :
    SQLiteOpenHelper(context, "ValorantPlayersWidgetUpdate.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $VALOPLAYERS ($PUUID TEXT PRIMARY KEY, $USERNAME TEXT, $USERTAG TEXT, $REGION TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun getPlayerName(): String? {
        var playerName: String? = null
        val sqlString = "SELECT $USERNAME, $USERTAG FROM $VALOPLAYERS"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(sqlString, null)
        if (cursor.moveToFirst()) {
            playerName = cursor.getString(0) + "#" + cursor.getString(1)
        }
        cursor.close()
        db.close()
        return playerName
    }

    fun logOutPlayer(userName: String): Boolean {
        val db = this.writableDatabase
        return try {
            db.execSQL("DELETE FROM $VALOPLAYERS WHERE $USERNAME = '$userName'")
            db.close()
            true
        } catch (e: Exception) {
            false
        }
    }


    fun addPlayer(gameName: String, gameTag: String, puuid: String, region: String): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(PUUID, puuid)
        cv.put(USERNAME, gameName)
        cv.put(USERTAG, gameTag)
        cv.put(REGION, region)
        val insert = db.insert(VALOPLAYERS, null, cv)
        db.close()
        return insert != -1L
    }

    // get puuid from the database given the gameName and gameTag
    fun getPUUID(gameName: String, gameTag: String): String? {
        var puuid: String? = null
        val sqlString =
            "SELECT $PUUID FROM $VALOPLAYERS WHERE $USERNAME = '$gameName' AND $USERTAG = '$gameTag'"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(sqlString, null)
        if (cursor.moveToFirst()) {
            puuid = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return puuid
    }

    fun getRegion(puuid: String?): String? {
        var region: String? = null
        val sqlString = "SELECT $REGION FROM $VALOPLAYERS WHERE $PUUID = '$puuid'"
        val db = this.readableDatabase
        val cursor: Cursor = db.rawQuery(sqlString, null)
        if (cursor.moveToFirst()) {
            region = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return region
    }

    companion object {
        const val PUUID = "PUUID"
        const val USERNAME = "USERNAME"
        const val USERTAG = "USERTAG"
        const val VALOPLAYERS = "VALOPLAYERS"
        const val REGION = "REGION"
    }

}
