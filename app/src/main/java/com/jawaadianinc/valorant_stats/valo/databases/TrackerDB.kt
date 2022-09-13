package com.jawaadianinc.valorant_stats.valo.databases

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class TrackerDB(context: Context) : SQLiteOpenHelper(context, "TrackerGG.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $TrackerGG (${playerName} TEXT PRIMARY KEY, $agentsJSON TEXT, $weaponsJSON TEXT, $mapsJSON TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun insertDetails(playerName: String, agents: String, weapons: String, maps: String): Boolean {
        val db = this.writableDatabase
        // insert into table and return true if successful
        return try {
            val query =
                "INSERT INTO $TrackerGG ('${Companion.playerName}', '${agentsJSON}', '${weaponsJSON}', '${mapsJSON}') VALUES (?,?,?,?)"
            val statement = db.compileStatement(query)
            statement.bindString(1, playerName)
            statement.bindString(2, agents)
            statement.bindString(3, weapons)
            statement.bindString(4, maps)
            val success = statement.executeInsert() != -1L
            db.close()
            return success
        } catch (e: Exception) {
            Log.d("TrackerGG", "Error inserting into database: $e")
            false
        }
    }

    fun getAgentsJSON(playerName: String): String? {
        val db = this.readableDatabase
        val query =
            "SELECT $agentsJSON FROM $TrackerGG WHERE ${Companion.playerName} = '$playerName'"
        val cursor = db.rawQuery(query, null)
        var agentsJSON: String? = null
        if (cursor.moveToFirst()) {
            agentsJSON = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return agentsJSON
    }

    fun getWeaponsJSON(playerName: String): String? {
        val db = this.readableDatabase
        val query =
            "SELECT $weaponsJSON FROM $TrackerGG WHERE ${Companion.playerName} = '$playerName'"
        val cursor = db.rawQuery(query, null)
        var weaponsJSON: String? = null
        if (cursor.moveToFirst()) {
            weaponsJSON = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return weaponsJSON
    }

    fun getMapsJSON(playerName: String): String? {
        val db = this.readableDatabase
        val query = "SELECT $mapsJSON FROM $TrackerGG WHERE ${Companion.playerName} = '$playerName'"
        val cursor = db.rawQuery(query, null)
        var mapsJSON: String? = null
        if (cursor.moveToFirst()) {
            mapsJSON = cursor.getString(0)
        }
        cursor.close()
        db.close()
        return mapsJSON
    }

    fun deleteDetails(playerName: String): Boolean {
        val db = this.writableDatabase
        return try {
            // empty the table with no rows
            val delete = "DELETE FROM $TrackerGG"
            db.execSQL(delete)
            db.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    companion object {
        const val TrackerGG = "TrackerGG"
        const val playerName = "playerName"
        const val agentsJSON = "agentsJSON"
        const val weaponsJSON = "weaponsJSON"
        const val mapsJSON = "mapsJSON"
    }

}
