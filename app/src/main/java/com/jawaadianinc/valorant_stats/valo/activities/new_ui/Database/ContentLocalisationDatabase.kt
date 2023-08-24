package com.jawaadianinc.valorant_stats.valo.activities.new_ui.Database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ContentLocalisationDatabase
    (context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    private val languageCodesDB = arrayOf(
        "ar_AE", "de_DE", "en_US", "es_ES", "es_MX", "fr_FR", "id_ID",
        "it_IT", "ja_JP", "ko_KR", "pl_PL", "pt_BR", "ru_RU", "th_TH", "tr_TR",
        "vi_VN", "zh_CN", "zh_TW"
    )

    companion object {
        private const val DATABASE_NAME = "ValorantTranslations.db"
        private const val DATABASE_VERSION = 1
        private const val UUID = "UUID"
        private const val translatedString = "translatedString"
    }

    override fun onCreate(db: SQLiteDatabase) {
        for (languageCode in languageCodesDB) {
            val createTableQuery =
                "CREATE TABLE IF NOT EXISTS $languageCode($UUID TEXT PRIMARY KEY, $translatedString TEXT)"
            db.execSQL(createTableQuery)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Handle database upgrade if needed
    }

    // Define a sealed class to represent progress updates
    sealed class Progress {
        object Started : Progress()
        data class Progressing(val current: Int, val total: Int) : Progress()
        object Completed : Progress()
    }

    suspend fun addString(
        lang: String,
        uuid: String,
        translate: String?,
        db: ContentLocalisationDatabase
    ) = withContext(Dispatchers.IO) {
        if (translate == null) return@withContext
        var cursor: Cursor? = null
        val database = db.writableDatabase

        try {
            val query = "SELECT * FROM $lang WHERE $UUID = '$uuid'"
            cursor = database.rawQuery(query, null)
            if (cursor.moveToFirst()) {
                return@withContext
            } else {
                val contentValues = ContentValues().apply {
                    put(UUID, uuid)
                    put(translatedString, translate)
                }
                val success = database.insert(lang, null, contentValues) != -1L
                contentValues.clear()
                return@withContext
            }
        } catch (e: Exception) {
            return@withContext
        } finally {
            cursor?.close()
        }
    }

    fun getTranslatedString(language: String, uuid: String): String? {
        val query = "SELECT translatedString FROM $language WHERE UUID = ?"
        val db = readableDatabase
        val cursor = db.rawQuery(query, arrayOf(uuid))

        var translatedString: String? = null

        if (cursor.moveToFirst()) {
            val index = cursor.getColumnIndex("translatedString")
            translatedString = cursor.getString(index)
        }

        cursor.close()
        db.close()

        return translatedString
    }
}
