package com.jawaadianinc.valorant_stats.valo.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TrackerDB(context: Context) : SQLiteOpenHelper(context, "TrackerGG.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $TrackerGG (${playerName} TEXT, $agentsJSON TEXT, $weaponsJSON TEXT, $mapsJSON TEXT, $timeInserted TEXT PRIMARY KEY, $gameMode TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun insertDetails(
        playerName: String,
        agents: String,
        weapons: String,
        maps: String,
        mode: String
    ): Boolean {
        val db = this.writableDatabase
        // insert into table and return true if successful
        return try {
            val time = System.currentTimeMillis()
            val query =
                "INSERT INTO $TrackerGG ('${Companion.playerName}', '${agentsJSON}', '${weaponsJSON}', '${mapsJSON}', '$timeInserted', $gameMode) VALUES (?,?,?,?,?,?)"
            val statement = db.compileStatement(query)
            statement.bindString(1, playerName)
            statement.bindString(2, agents)
            statement.bindString(3, weapons)
            statement.bindString(4, maps)
            statement.bindString(5, time.toString())
            statement.bindString(6, mode)
            val success = statement.executeInsert() != -1L
            db.close()
            return success
        } catch (e: Exception) {
            Log.d("TrackerGG", "Error inserting into database: $e")
            false
        }
    }

    fun getAgentsJSON(playerName: String, gameMode: String): String? {
        val db = this.readableDatabase
        val query =
            "SELECT $agentsJSON FROM $TrackerGG WHERE ${Companion.playerName} = '$playerName' AND ${Companion.gameMode} = '$gameMode'"
        val cursor = db.rawQuery(query, null)
        var agentsJSON: String? = null
        if (cursor.moveToFirst()) {
            agentsJSON = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return agentsJSON
    }

    fun getWeaponsJSON(playerName: String, gameMode: String): String? {
        val db = this.readableDatabase
        val query =
            "SELECT $weaponsJSON FROM $TrackerGG WHERE ${Companion.playerName} = '$playerName' AND ${Companion.gameMode} = '$gameMode'"
        val cursor = db.rawQuery(query, null)
        var weaponsJSON: String? = null
        if (cursor.moveToFirst()) {
            weaponsJSON = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return weaponsJSON
    }

    fun getMapsJSON(playerName: String, gameMode: String): String? {
        val db = this.readableDatabase
        val query =
            "SELECT $mapsJSON FROM $TrackerGG WHERE ${Companion.playerName} = '$playerName' AND ${Companion.gameMode} = '$gameMode'"
        val cursor = db.rawQuery(query, null)
        var mapsJSON: String? = null
        if (cursor.moveToFirst()) {
            mapsJSON = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return mapsJSON
    }

    fun deleteDetails(mode: String, playerName: String): Boolean {
        val db = this.writableDatabase
        return try {
            // empty the table with no rows
            val delete =
                "DELETE FROM $TrackerGG WHERE $gameMode = '$mode' AND ${Companion.playerName} = '$playerName'"
            db.execSQL(delete)
            db.close()
            true
        } catch (e: Exception) {
            Log.d("TrackerGG", "Error deleting from database: $e")
            false
        }
    }

    fun checkIfNewDataNeeded(mode: String, playerName: String): Boolean {
        val db = this.readableDatabase
        // check if the time started is more than 1 day ago
        val query =
            "SELECT $timeInserted FROM $TrackerGG WHERE ${Companion.playerName} = '$playerName' AND $gameMode = '$mode'"
        val cursor = db.rawQuery(query, null)
        var timeInserted: Long = 0
        if (cursor.moveToFirst()) {
            timeInserted = cursor.getString(0).toLong()
        }
        cursor.close()
        db.close()
        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - timeInserted
        val oneDay = 86400000
        // if the time difference is more than 1 day, return true
        return timeDifference > oneDay
    }

    fun checkIfDataExists(mode: String, playerName: String): Boolean {
        val db = this.readableDatabase
        val query =
            "SELECT * FROM $TrackerGG WHERE ${Companion.playerName} = '$playerName' AND $gameMode = '$mode'"
        val cursor = db.rawQuery(query, null)
        val exists = cursor.moveToFirst()
        cursor.close()
        db.close()
        return exists
    }

    fun updateDetails(
        playerName: String,
        agents: String,
        weapons: String,
        maps: String,
        mode: String
    ): Boolean {
        val db = this.writableDatabase
        // insert into table and return true if successful
        return try {
            val time = System.currentTimeMillis()
            val query =
                "UPDATE $TrackerGG SET $agentsJSON = '$agents', $weaponsJSON = '$weapons', $mapsJSON = '$maps', $timeInserted = '$time' WHERE ${Companion.playerName} = '$playerName' AND $gameMode = '$mode'"
            db.execSQL(query)
            db.close()
            true
        } catch (e: Exception) {
            Log.d("TrackerGG", "Error updating database: $e")
            false
        }
    }

    companion object {
        const val TrackerGG = "TrackerGG"
        const val playerName = "playerName"
        const val agentsJSON = "agentsJSON"
        const val weaponsJSON = "weaponsJSON"
        const val mapsJSON = "mapsJSON"
        const val timeInserted = "timeInserted"
        const val gameMode = "gameMode"
    }

}
