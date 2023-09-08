package com.trailsi.malmo.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trailsi.malmo.android.common.db.SQLiteHelper;

import java.io.Serializable;

public class AudioType implements Serializable {
    public int id;
    public String name;
    public String android_product_id;
    public String ios_product_id;
    public boolean hide_list;
    public String geo_json;

    public AudioType() {

    }

    public AudioType(Cursor c) {
        readFromCursor(c);
    }

    public static final String TABLE_NAME = "AudioType";
    public static final String[] COLUMN = new String[]{"*"};

    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s("
                    + "id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "name VARCHAR(255),"
                    + "android_product_id VARCHAR(100),"
                    + "ios_product_id VARCHAR(100),"
                    + "hide_list INTEGER DEFAULT 0,"
                    + "geo_json TEXT"
                    + ")", TABLE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues prepareContentValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("android_product_id", android_product_id);
        contentValues.put("ios_product_id", ios_product_id);
        contentValues.put("hide_list", hide_list);
        contentValues.put("geo_json", geo_json);
        return contentValues;
    }

    public void readFromCursor(Cursor c) {
        id = SQLiteHelper.getInt(c, "id");
        name = SQLiteHelper.getString(c, "name");
        android_product_id = SQLiteHelper.getString(c, "android_product_id");
        ios_product_id = SQLiteHelper.getString(c, "ios_product_id");
        hide_list = SQLiteHelper.getInt(c, "hide_list") == 1;
        geo_json = SQLiteHelper.getString(c, "geo_json");
    }
}
