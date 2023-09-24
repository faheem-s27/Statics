package com.jawaadianinc.valorant_stats.main

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class ValorantAccountDatabase(context: Context) :
    SQLiteOpenHelper(context, "ValorantAccountDatabase_Rank.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $ACCOUNTS ($PUUID TEXT PRIMARY KEY, $NAME TEXT, $IMAGEID TEXT, $COOKIES TEXT)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun deletePlayer(puuid: String): Boolean {
        val db = this.writableDatabase
        return try {
            db.execSQL("DELETE FROM $ACCOUNTS WHERE $PUUID = '$puuid'")
            db.close()
            true
        } catch (e: Exception) {
            false
        }
    }

    fun addPlayer(gameName: String, cookies: String, puuid: String, imageID: String): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(PUUID, puuid)
        cv.put(NAME, gameName)
        cv.put(COOKIES, cookies)
        cv.put(IMAGEID, imageID)
        val insert = db.insert(ACCOUNTS, null, cv)
        db.close()
        return insert != -1L
    }

    fun doesPlayerExist(puuid: String): Boolean {
        val db = this.readableDatabase

        val playerExistsQuery = "SELECT $PUUID FROM $ACCOUNTS WHERE $PUUID = ?"
        val cursor = db.rawQuery(playerExistsQuery, arrayOf(puuid))
        val playerExists = cursor.moveToFirst()

        cursor.close()
        db.close()

        return playerExists
    }

    fun updatePlayerImage(puuid: String, newImageID: String): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(IMAGEID, newImageID)

        val updateResult = db.update(ACCOUNTS, cv, "$PUUID=?", arrayOf(puuid))
        db.close()

        return updateResult > 0
    }

    fun updatePlayerCookies(puuid: String, cookies: String): Boolean {
        val db = this.writableDatabase
        val cv = ContentValues()
        cv.put(COOKIES, cookies)

        val updateResult = db.update(ACCOUNTS, cv, "$PUUID=?", arrayOf(puuid))
        db.close()

        return updateResult > 0
    }

    fun getAllValorantAccounts(): List<ValorantAccount> {
        val accountList = mutableListOf<ValorantAccount>()
        val db = this.readableDatabase

        val query = "SELECT * FROM $ACCOUNTS"
        val cursor = db.rawQuery(query, null)

        while (cursor.moveToNext()) {
            val nameIndex = cursor.getColumnIndex(NAME)
            val imageIDIndex = cursor.getColumnIndex(IMAGEID)
            val cookiesIndex = cursor.getColumnIndex(COOKIES)
            val puuidIndex = cursor.getColumnIndex(PUUID)

            if (nameIndex != -1 && imageIDIndex != -1 && cookiesIndex != -1 && puuidIndex != -1) {
                val name = cursor.getString(nameIndex)
                val imageID = cursor.getString(imageIDIndex)
                val cookies = cursor.getString(cookiesIndex)
                val puuid = cursor.getString(puuidIndex)
                val account = ValorantAccount(name, imageID, cookies, puuid)
                accountList.add(account)
            }
        }

        cursor.close()
        db.close()

        return accountList
    }

    companion object {
        const val ACCOUNTS = "ACCOUNTS"
        const val PUUID = "PUUID"
        const val NAME = "NAME"
        const val IMAGEID = "IMAGEID"
        const val COOKIES = "COOKIES"
    }

}
