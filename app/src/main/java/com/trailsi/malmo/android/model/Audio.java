package com.trailsi.malmo.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trailsi.malmo.android.common.db.SQLiteHelper;

import java.io.Serializable;
import java.util.List;

public class Audio implements Serializable {
    public int id;
    public int location_number;
    public String audio;
    public String type;
    public String language;
    public boolean is_adult;

    public List<AudioAds> ads;

    public Audio(Cursor c) {
        readFromCursor(c);
    }

    public static final String TABLE_NAME = "Audio";
    public static final String[] COLUMN = new String[]{"*"};

    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s("
                    + "id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "location_number INTEGER DEFAULT 0,"
                    + "audio VARCHAR(255),"
                    + "type VARCHAR(255),"
                    + "language VARCHAR(255),"
                    + "is_adult INTEGER DEFAULT 0"
                    + ")", TABLE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues prepareContentValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("location_number", location_number);
        contentValues.put("audio", audio);
        contentValues.put("type", type);
        contentValues.put("language", language);
        contentValues.put("is_adult", is_adult);
        return contentValues;
    }

    public void readFromCursor(Cursor c) {
        id = SQLiteHelper.getInt(c, "id");
        location_number = SQLiteHelper.getInt(c, "location");
        audio = SQLiteHelper.getString(c, "audio");
        type = SQLiteHelper.getString(c, "type");
        language = SQLiteHelper.getString(c, "language");
        is_adult = SQLiteHelper.getInt(c, "is_adult") == 1;
    }
}
