package com.trailsi.malmo.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trailsi.malmo.android.common.db.SQLiteHelper;

import java.io.Serializable;

public class Document implements Serializable {
    public int id;
    public String name;
    public String file;

    public Document(Cursor c) {
        readFromCursor(c);
    }

    public static final String TABLE_NAME = "Document";
    public static final String[] COLUMN = new String[]{"*"};

    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s("
                    + "id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "name VARCHAR(100),"
                    + "file VARCHAR(255)"
                    + ")", TABLE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues prepareContentValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", name);
        contentValues.put("file", file);
        return contentValues;
    }

    public void readFromCursor(Cursor c) {
        id = SQLiteHelper.getInt(c, "id");
        name = SQLiteHelper.getString(c, "name");
        file = SQLiteHelper.getString(c, "file");
    }
}
