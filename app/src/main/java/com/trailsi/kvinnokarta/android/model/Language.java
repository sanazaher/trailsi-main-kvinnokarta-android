package com.trailsi.kvinnokarta.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trailsi.kvinnokarta.android.common.db.SQLiteHelper;

import java.io.Serializable;

public class Language implements Serializable {
    public int id;
    public String isoCode;
    public String language;

    public Language() {

    }

    public Language(String isoCode, String language) {
        this.isoCode = isoCode;
        this.language = language;
    }

    public Language(Cursor c) {
        readFromCursor(c);
    }

    public static final String TABLE_NAME = "Language";
    public static final String[] COLUMN = new String[]{"*"};

    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s("
                    + "id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "isoCode VARCHAR(100),"
                    + "language VARCHAR(255)"
                    + ")", TABLE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues prepareContentValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("isoCode", isoCode);
        contentValues.put("language", language);
        return contentValues;
    }

    public void readFromCursor(Cursor c) {
        id = SQLiteHelper.getInt(c, "id");
        isoCode = SQLiteHelper.getString(c, "isoCode");
        language = SQLiteHelper.getString(c, "language");
    }
}
