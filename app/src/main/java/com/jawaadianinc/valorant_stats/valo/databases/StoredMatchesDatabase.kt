package com.jawaadianinc.valorant_stats.valo.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class StoredMatchesDatabase(context: Context) :
    SQLiteOpenHelper(context, "PlayerMatches.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $PlayerMatches (${TimeStarted} TEXT PRIMARY KEY, $MatchID TEXT, $MatchJSON TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun addMatch(timeStarted: String, matchID: String, matchJSON: String): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(TimeStarted, timeStarted)
        cv.put(MatchID, matchID)
        cv.put(MatchJSON, matchJSON)
        val result = db.insert(PlayerMatches, null, cv)
        db.close()
        return result != (-1).toLong()
    }

    fun addListOfMatches(listOfMatches: List<Match>) {
        val db = this.writableDatabase
        val cv = ContentValues()
        for (match in listOfMatches) {
            cv.put(TimeStarted, match.timeStarted)
            cv.put(MatchID, match.matchID)
            cv.put(MatchJSON, match.matchJSON)
            db.insert(PlayerMatches, null, cv)
        }
        db.close()
    }

    fun isinDatabase(timeStarted: String, matchID: String): Boolean {
        val db = this.readableDatabase
        val query =
            "SELECT * FROM $PlayerMatches WHERE $TimeStarted = '$timeStarted' AND $MatchID = '$matchID'"
        val cursor = db.rawQuery(query, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count > 0
    }

    fun getJSON(timeStarted: String, matchID: String): String? {
        val db = this.readableDatabase
        val query =
            "SELECT $MatchJSON FROM $PlayerMatches WHERE $TimeStarted = '$timeStarted' AND $MatchID = '$matchID'"
        val cursor: Cursor = db.rawQuery(query, null)

        // If the cursor is empty, return null
        if (cursor.count == 0) {
            cursor.close()
            db.close()
            return null
        }

        cursor.moveToFirst()
        val json = cursor.getString(0)
        cursor.close()
        db.close()
        return json
    }

    fun getLatestMatch(): String {
        // return the json from where the timestarted is the latest
        val db = this.readableDatabase
        val query = "SELECT * FROM $PlayerMatches ORDER BY $TimeStarted DESC LIMIT 1"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val json = cursor.getString(2)
        cursor.close()
        db.close()
        return json

    }

    fun getAllMatches(): List<Match> {
        val db = this.readableDatabase
        val query = "SELECT * FROM $PlayerMatches"
        val cursor = db.rawQuery(query, null)
        val listOfMatches = mutableListOf<Match>()
        if (cursor.count == 0) {
            cursor.close()
            db.close()
            return listOfMatches
        }
        while (cursor.moveToNext()) {
            val timeStarted = cursor.getString(0)
            val matchID = cursor.getString(1)
            val matchJSON = cursor.getString(2)
            listOfMatches.add(Match(timeStarted, matchID, matchJSON))
        }
        cursor.close()
        db.close()
        return listOfMatches
    }

    fun numberOfMatches(): Int {
        val db = this.readableDatabase
        val query = "SELECT * FROM $PlayerMatches"
        val cursor = db.rawQuery(query, null)
        val count = cursor.count
        cursor.close()
        db.close()
        return count
    }

    fun deleteOldestMatch() {
        // get the oldest match and delete it
        val db = this.writableDatabase
        val query = "SELECT * FROM $PlayerMatches ORDER BY $TimeStarted ASC LIMIT 1"
        val cursor = db.rawQuery(query, null)
        cursor.moveToFirst()
        val timeStarted = cursor.getString(0)
        val matchID = cursor.getString(1)
        db.delete(PlayerMatches, "$TimeStarted = ? AND $MatchID = ?", arrayOf(timeStarted, matchID))
        cursor.close()
        db.close()
    }

    companion object {
        const val PlayerMatches = "PlayerMatches"
        const val TimeStarted = "TimeStarted"
        const val MatchID = "MatchID"
        const val MatchJSON = "MatchJSON"
    }
}
