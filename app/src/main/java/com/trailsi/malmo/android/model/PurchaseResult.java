package com.trailsi.malmo.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trailsi.malmo.android.common.db.SQLiteHelper;

import java.io.Serializable;

public class PurchaseResult implements Serializable {
    public int id;
    public String type;
    public String sku;
    public String purchase_token;
    public boolean purchased;

    public PurchaseResult() {

    }

    public PurchaseResult(Cursor c) {
        readFromCursor(c);
    }

    public static final String TABLE_NAME = "PurchaseResult";
    public static final String[] COLUMN = new String[]{"*"};

    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s("
                    + "id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "type VARCHAR(255),"
                    + "sku VARCHAR(100),"
                    + "purchase_token VARCHAR(100),"
                    + "purchased INTEGER DEFAULT 0"
                    + ")", TABLE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues prepareContentValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("type", type);
        contentValues.put("sku", sku);
        contentValues.put("purchase_token", purchase_token);
        contentValues.put("purchased", purchased);
        return contentValues;
    }

    public void readFromCursor(Cursor c) {
        id = SQLiteHelper.getInt(c, "id");
        type = SQLiteHelper.getString(c, "type");
        sku = SQLiteHelper.getString(c, "sku");
        purchase_token = SQLiteHelper.getString(c, "purchase_token");
        purchased = SQLiteHelper.getInt(c, "purchased") == 1;
    }
}
