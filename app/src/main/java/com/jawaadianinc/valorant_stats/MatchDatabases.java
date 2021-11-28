package com.jawaadianinc.valorant_stats;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class MatchDatabases extends SQLiteOpenHelper {

    public static final String MATCH_ID = "MATCH_ID";
    public static final String USER = "USER";
    public static final String USERMATCHES = "USERMATCHES";
    public static final String MAP = "MAP";
    public static final String GAMEMODE = "GAMEMODE";

    public MatchDatabases(@NotNull Context context) {
        super(context, "matches.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + USERMATCHES + " (" + MATCH_ID + " TEXT PRIMARY KEY, " + USER + " TEXT, " + MAP + " TEXT, " + GAMEMODE + " TEXT)";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

    public boolean addMatches(String matchID, String User, String Map, String Gamemode) {
        //Check if matchID exists in database
        SQLiteDatabase database = this.getReadableDatabase();
        String SQLString = "SELECT MATCH_ID FROM " + USERMATCHES + " WHERE " + MATCH_ID + " = '" + matchID + "'";
        Cursor cursor = database.rawQuery(SQLString, null);
        if (cursor.moveToFirst()) {
            cursor.close();
            database.close();
            return false;
        }

        //Execute if match isnt in database
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(MATCH_ID, matchID);
        cv.put(GAMEMODE, Gamemode);
        cv.put(MAP, Map);
        cv.put(USER, User);
        final long insert = db.insert(USERMATCHES, null, cv);
        db.close();
        return insert != -1;
    }

    public ArrayList<String> getMatches(String User) {
        ArrayList<String> matchesID = new ArrayList<>();
        String SQLString = "SELECT " + MATCH_ID + " FROM " + USERMATCHES + " WHERE " + USER + " = '" + User + "'";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(SQLString, null);
        if (cursor.moveToFirst())
            do {
                String MatchID = cursor.getString(0);
                matchesID.add(MatchID);
            } while (cursor.moveToNext());
        else {
            return null;
        }
        cursor.close();
        db.close();
        return matchesID;
    }

}
