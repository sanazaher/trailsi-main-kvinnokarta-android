package com.trailsi.kvinnokarta.android.model;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.trailsi.kvinnokarta.android.common.db.SQLiteHelper;

import java.io.Serializable;
import java.util.List;

public class Location implements Serializable {
    public int id;
    public String uuid;
    public int number;
    public String name;
    public String image;
    public int location_type;
    public String beacon_major;
    public String beacon_minor;
    public double latitude;
    public double longitude;
    public double radius;
    public boolean visibility;

    public List<Audio> audios;

    public Location() {

    }

    public Location(Cursor c) {
        readFromCursor(c);
    }

    public static final String TABLE_NAME = "Location";
    public static final String[] COLUMN = new String[]{"*"};

    public static void createTable(SQLiteDatabase db) {
        try {
            db.execSQL(String.format("CREATE TABLE IF NOT EXISTS %s("
                    + "id INTEGER UNIQUE PRIMARY KEY AUTOINCREMENT NOT NULL,"
                    + "uuid VARCHAR(100),"
                    + "number INTEGER DEFAULT 0,"
                    + "name VARCHAR(100),"
                    + "image VARCHAR(255),"
                    + "location_type INTEGER DEFAULT 0,"
                    + "beacon_major VARCHAR(100),"
                    + "beacon_minor VARCHAR(100),"
                    + "latitude REAL,"
                    + "longitude REAL,"
                    + "radius REAL,"
                    + "visibility INTEGER DEFAULT 1"
                    + ")", TABLE_NAME));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public ContentValues prepareContentValue() {
        ContentValues contentValues = new ContentValues();
        contentValues.put("uuid", uuid);
        contentValues.put("number", number);
        contentValues.put("name", name);
        contentValues.put("image", image);
        contentValues.put("location_type", location_type);
        contentValues.put("beacon_major", beacon_major);
        contentValues.put("beacon_minor", beacon_minor);
        contentValues.put("latitude", latitude);
        contentValues.put("longitude", longitude);
        contentValues.put("radius", radius);
        contentValues.put("visibility", visibility);
        return contentValues;
    }

    public void readFromCursor(Cursor c) {
        id = SQLiteHelper.getInt(c, "id");
        uuid = SQLiteHelper.getString(c, "uuid");
        number = SQLiteHelper.getInt(c, "number");
        name = SQLiteHelper.getString(c, "name");
        image = SQLiteHelper.getString(c, "image");
        location_type = SQLiteHelper.getInt(c, "location_type");
        beacon_major = SQLiteHelper.getString(c, "beacon_major");
        beacon_minor = SQLiteHelper.getString(c, "beacon_minor");
        latitude = SQLiteHelper.getDouble(c, "latitude");
        longitude = SQLiteHelper.getDouble(c, "longitude");
        radius = SQLiteHelper.getDouble(c, "radius");
        visibility = SQLiteHelper.getInt(c, "visibility") == 1;
    }
}
