package com.jawaadianinc.valorant_stats.valo.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class MatchDatabase(context: Context) : SQLiteOpenHelper(context, "LastMatch.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $LastMatches ($MatchID TEXT PRIMARY KEY, $MatchJSON TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun insertMatch(matchID: String, matchJSON: String): Boolean {
        // check if there's any matches in the database
        // if there is, delete them
        val db = this.writableDatabase
        val query = "SELECT * FROM $LastMatches"
        val cursor = db.rawQuery(query, null)
        if (cursor.count > 0) {
            db.execSQL("DELETE FROM $LastMatches")
        }
        cursor.close()
        // insert the new match
        val insert = "INSERT INTO $LastMatches ($MatchID, $MatchJSON) VALUES (?,?)"
        val statement = db.compileStatement(insert)
        statement.bindString(1, matchID)
        statement.bindString(2, matchJSON)

        // return if the insert was successful
        val success = statement.executeInsert() != -1L
        db.close()
        return success
    }

    fun checkForAnyMatches(): String? {
        // check if there is a match in the database, return the json if there is
        val db = this.readableDatabase
        val query = "SELECT * FROM $LastMatches"
        val cursor = db.rawQuery(query, null)
        return if (cursor.count > 0) {
            cursor.moveToFirst()
            val matchJSON = cursor.getString(1)
            cursor.close()
            db.close()
            matchJSON
        } else {
            cursor.close()
            db.close()
            null
        }
    }

    fun deleteMatch() {
        // delete the match from the database
        val db = this.writableDatabase
        db.execSQL("DELETE FROM $LastMatches")
        db.close()
    }

    companion object {
        const val LastMatches = "LastMatches"
        const val MatchID = "MatchID"
        const val MatchJSON = "MatchJSON"
    }
}
