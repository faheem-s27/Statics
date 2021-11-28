package com.jawaadianinc.valorant_stats

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.util.*

class MatchDatabases(context: Context) : SQLiteOpenHelper(context, "matches.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $USERMATCHES (ID INTEGER PRIMARY KEY AUTOINCREMENT, $MATCH_ID TEXT, $USER TEXT, $MAP TEXT, $GAMEMODE TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun addMatches(matchID: String, User: String?, Map: String?, Gamemode: String?): Boolean {
        //Check if matchID exists in database
        val database = this.readableDatabase
        val sqlString =
            "SELECT MATCH_ID FROM $USERMATCHES WHERE $MATCH_ID = '$matchID' AND $USER = '$User'"
        val cursor = database.rawQuery(sqlString, null)
        if (cursor.moveToFirst()) {
            cursor.close()
            database.close()
            return false
        }

        //Execute if match isn't in database
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(MATCH_ID, matchID)
        cv.put(GAMEMODE, Gamemode)
        cv.put(MAP, Map)
        cv.put(USER, User)
        val insert = db.insert(USERMATCHES, null, cv)
        db.close()
        return insert != -1L
    }

    fun getMatches(User: String): ArrayList<String>? {
        val matchesID = ArrayList<String>()
        val sqlString =
            "SELECT $MATCH_ID FROM $USERMATCHES WHERE $USER = '$User'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(sqlString, null)
        if (cursor.moveToFirst()) do {
            val matchID = cursor.getString(0)
            matchesID.add(matchID)
        } while (cursor.moveToNext()) else {
            return null
        }
        cursor.close()
        db.close()
        return matchesID
    }

    fun getTotalMatches() {

    }

    fun getTotalUserMatches(User: String): Int {
        var numberofMatches = 0
        val sqlString = "SELECT * FROM $USERMATCHES WHERE $USER = '$User'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(sqlString, null)
        if (cursor.moveToFirst())
            do {
                numberofMatches += 1
            } while (cursor.moveToNext())

        cursor.close()
        db.close()
        return numberofMatches
    }

    fun getallMatches(User: String): ArrayList<String> {
        val matches = ArrayList<String>()
        val sqlString = "SELECT $MAP, $GAMEMODE, $MATCH_ID FROM $USERMATCHES WHERE $USER = '$User'"
        val db = this.readableDatabase
        val cursor = db.rawQuery(sqlString, null)
        if (cursor.moveToFirst())
            do {
                matches.add(
                    cursor.getString(0) + " " + cursor.getString(1) + ":\n" + cursor.getString(
                        2
                    )
                )
            } while (cursor.moveToNext())

        cursor.close()
        db.close()
        return matches
    }


    companion object {
        const val MATCH_ID = "MATCH_ID"
        const val USER = "USER"
        const val USERMATCHES = "USERMATCHES"
        const val MAP = "MAP"
        const val GAMEMODE = "GAMEMODE"
    }
}