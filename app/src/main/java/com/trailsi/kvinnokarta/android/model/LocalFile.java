package com.trailsi.kvinnokarta.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trailsi.kvinnokarta.android.common.db.SQLiteHelper;

import java.io.Serializable;

public class LocalFile implements Serializable {
    public int id;
    public String cloud_link;
    public String local_file;

    public LocalFile() {

    }

    public LocalFile(String cloud_link, String local_file) {
        this.cloud_link = cloud_link;
        this.local_file = local_file;
    }

    public LocalFile(Cursor c) {
        readFromCursor(c);
    }

    public static final String TABLE_NAME = "LocalFile";
    public static final String[] COLUMN = new String[]{"*"};

    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s("
                    + "id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "cloud_link VARCHAR(100),"
                    + "local_file VARCHAR(255)"
                    + ")", TABLE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues prepareContentValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("cloud_link", cloud_link);
        contentValues.put("local_file", local_file);
        return contentValues;
    }

    public void readFromCursor(Cursor c) {
        id = SQLiteHelper.getInt(c, "id");
        cloud_link = SQLiteHelper.getString(c, "cloud_link");
        local_file = SQLiteHelper.getString(c, "local_file");
    }
}
