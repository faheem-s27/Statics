package com.jawaadianinc.valorant_stats.valo.databases

import android.content.ContentValues
import android.content.Context
import android.database.DatabaseUtils
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream


class AssetsDatabase(context: Context) : SQLiteOpenHelper(context, "ValoAssets.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        val createTable =
            "CREATE TABLE $ValoAssets (${UUID} TEXT PRIMARY KEY, $Type TEXT, $Name TEXT, $Image BLOB)"
        db.execSQL(createTable)
    }

    override fun onUpgrade(sqLiteDatabase: SQLiteDatabase, i: Int, i1: Int) {}

    fun addData(UUID: String, Type: String, Name: String, Image: Bitmap): Boolean {
        val db = this.writableDatabase
        val contentValues = ContentValues()
        // format the name with no apostrophes
        val formattedName = Name.replace("'", "")
        contentValues.put(Companion.UUID, UUID)
        contentValues.put(Companion.Type, Type)
        contentValues.put(Companion.Name, formattedName)
        contentValues.put(Companion.Image, getBitmapAsByteArray(Image))
        val success = db.insert(ValoAssets, null, contentValues) != -1L
        // close the database
        contentValues.clear()
        db.close()

        // Return true if the insertion was successful
        return success
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
        val db: SQLiteDatabase = this.readableDatabase
        val count = DatabaseUtils.queryNumEntries(db, ValoAssets)
        db.close()
        return count.toInt()
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

    fun checkForExisting(UUID: String, Name: String): Boolean {
        // format the Name so there is no apostrophe
        val formattedName = Name.replace("'", "")
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $ValoAssets WHERE ${Companion.UUID} = '$UUID' AND ${Companion.Name} = '$formattedName'",
            null
        )
        val exists = cursor.count > 0
        cursor.close()
        db.close()
        return exists
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
}
