package com.jawaadianinc.valorant_stats.valo.activities.new_ui.Database

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class ContentLocalisationDatabase(context: Context) :
    SQLiteOpenHelper(context, "ValoAssetsTranslations.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $ValoAssetsTranslations (${Verison} TEXT PRIMARY KEY, $Data TEXT)"
        db.execSQL(createTable)
    }


    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    suspend fun addData(version: String, data: String): Boolean = withContext(Dispatchers.IO) {
        val db = this@ContentLocalisationDatabase.writableDatabase

        val values = ContentValues().apply {
            put(Verison, version)
            put(Data, data)
        }

        val result = db.insertWithOnConflict(
            ValoAssetsTranslations,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )

        result != -1L // Returns true if insertion was successful, false otherwise
    }


    suspend fun getVersion(): String = withContext(Dispatchers.IO) {
        val db = this@ContentLocalisationDatabase.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $Verison FROM $ValoAssetsTranslations",
            null
        )

        var version = ""
        if (cursor.moveToFirst()) {
            version = cursor.getString(cursor.getColumnIndexOrThrow(Verison))
        }

        cursor.close()
        version
    }

    suspend fun getData(): String = withContext(Dispatchers.IO) {
        val db = this@ContentLocalisationDatabase.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $Data FROM $ValoAssetsTranslations",
            null
        )

        var data = ""
        if (cursor.moveToFirst()) {
            data = cursor.getString(cursor.getColumnIndexOrThrow(Data))
        }

        cursor.close()
        data
    }


    fun getNumberOfRows(): Int {
        val db: SQLiteDatabase = this.writableDatabase
        return db.use {
            DatabaseUtils.queryNumEntries(it, ValoAssetsTranslations).toInt()
        }
    }


    companion object {
        const val ValoAssetsTranslations = "ValoAssetsTranslations"
        const val Verison = "Version"
        const val Data = "Data"
    }

}
