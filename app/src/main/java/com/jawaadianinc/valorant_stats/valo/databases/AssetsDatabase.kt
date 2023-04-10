package com.jawaadianinc.valorant_stats.valo.databases

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream


class AssetsDatabase(context: Context) : SQLiteOpenHelper(context, "ValoAssets.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $ValoAssets (${UUID} TEXT PRIMARY KEY, $Type TEXT, $Name TEXT, $Image BLOB)"
        db.execSQL(createTable)
    }


    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    suspend fun checkAndAddData(
        uuid: String,
        type: String,
        name: String,
        image: Bitmap,
        db: AssetsDatabase
    ): Boolean = withContext(Dispatchers.IO) {
        val formattedName = name.replace("'", "")
        var cursor: Cursor? = null
        val database: SQLiteDatabase = db.writableDatabase
        try {
            cursor = database.rawQuery(
                "SELECT * FROM $ValoAssets WHERE $UUID = '$uuid' AND $Name = '$formattedName'",
                null
            )
            if (cursor.moveToFirst()) {
                // asset already exists in the database
                return@withContext true
            } else {
                // asset doesn't exist in the database, insert it
                val contentValues = ContentValues().apply {
                    put(UUID, uuid)
                    put(Type, type)
                    put(Name, formattedName)
                    put(Image, getBitmapAsByteArray(image))
                }
                val success = database.insert(ValoAssets, null, contentValues) != -1L
                contentValues.clear()
                return@withContext success
            }

        } catch (e: Exception) {
            Log.d("AssetsDatabase", "Error checking for existing asset $type $name", e)
            return@withContext false
        } finally {
            cursor?.close()
        }
    }

    fun retrieveImage(UUID: String, Name: String): Bitmap {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $Image FROM $ValoAssets WHERE ${Companion.UUID} = '$UUID' AND $Companion.Name = '$Name'",
            null
        )
        cursor.moveToFirst()
        val imgByte = cursor.getBlob(0)
        cursor.close()
        db.close()
        return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size)
    }

    fun retrieveImageUUID(UUID: String): Bitmap {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $Image FROM $ValoAssets WHERE ${Companion.UUID} = '$UUID'",
            null
        )
        cursor.moveToFirst()
        val imgByte = cursor.getBlob(0)
        cursor.close()
        db.close()
        return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size)
    }

    fun retrieveName(UUID: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $Name FROM $ValoAssets WHERE ${Companion.UUID} = '$UUID'",
            null
        )
        cursor.moveToFirst()
        val name = cursor.getString(0)
        cursor.close()
        db.close()
        return name
    }

    fun getNumberOfRows(): Int {
        val db: SQLiteDatabase = this.writableDatabase
        return db.use {
            DatabaseUtils.queryNumEntries(it, ValoAssets).toInt()
        }
    }

    // retrieve image from name only
    fun retrieveImage(Name: String): Bitmap {
        val db = this.readableDatabase
        val cursor =
            db.rawQuery("SELECT $Image FROM $ValoAssets WHERE ${Companion.Name} = '$Name'", null)
        cursor.moveToFirst()
        val imgByte = cursor.getBlob(0)
        cursor.close()
        db.close()
        return BitmapFactory.decodeByteArray(imgByte, 0, imgByte.size)
    }


    companion object {
        const val ValoAssets = "ValoAssets"
        const val Type = "Type"
        const val Name = "Name"
        const val UUID = "UUID"
        const val Image = "Image"
    }


    private fun getBitmapAsByteArray(bitmap: Bitmap): ByteArray? {
        val outputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        return outputStream.toByteArray()
    }

    fun retrieveAllTitles(): ArrayList<String> {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT $Name FROM $ValoAssets WHERE $Type = 'Title'", null)
        val titles = ArrayList<String>()
        while (cursor.moveToNext()) {
            titles.add(cursor.getString(0))
        }
        cursor.close()
        db.close()
        return titles
    }

    fun retrieveIDTitle(TitleName: String): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT $UUID FROM $ValoAssets WHERE $Type = 'Title' AND $Name = '$TitleName'",
            null
        )
        cursor.moveToFirst()
        val id = cursor.getString(0)
        cursor.close()
        db.close()
        return id
    }
}
