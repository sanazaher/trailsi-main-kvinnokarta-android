package com.trailsi.kvinnokarta.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trailsi.kvinnokarta.android.common.db.SQLiteHelper;

import java.io.Serializable;

public class AudioAds implements Serializable {
    public int id;
    public long audio_id;
    public String audio_position;
    public long position;
    public String url;

    public AudioAds(Cursor c) {
        readFromCursor(c);
    }

    public static final String TABLE_NAME = "AudioAds";
    public static final String[] COLUMN = new String[]{"*"};

    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s("
                    + "id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "audio_id INTEGER DEFAULT 0,"
                    + "audio_position VARCHAR(255),"
                    + "position INTEGER DEFAULT 0,"
                    + "url VARCHAR(255)"
                    + ")", TABLE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues prepareContentValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("audio_id", audio_id);
        contentValues.put("audio_position", audio_position);
        contentValues.put("position", position);
        contentValues.put("url", url);
        return contentValues;
    }

    public void readFromCursor(Cursor c) {
        id = SQLiteHelper.getInt(c, "id");
        audio_id = SQLiteHelper.getInt(c, "audio_id");
        audio_position = SQLiteHelper.getString(c, "audio_position");
        position = SQLiteHelper.getInt(c, "position");
        url = SQLiteHelper.getString(c, "url");
    }
}
