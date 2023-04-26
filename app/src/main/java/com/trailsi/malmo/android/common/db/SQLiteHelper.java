package com.trailsi.malmo.android.common.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.trailsi.malmo.android.common.utils.StringHelper;
import com.trailsi.malmo.android.model.Audio;
import com.trailsi.malmo.android.model.AudioAds;
import com.trailsi.malmo.android.model.AudioType;
import com.trailsi.malmo.android.model.Document;
import com.trailsi.malmo.android.model.Language;
import com.trailsi.malmo.android.model.LocalFile;
import com.trailsi.malmo.android.model.Location;
import com.trailsi.malmo.android.model.PurchaseResult;

public class SQLiteHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLiteHelper";

    public static final int DATABASE_VERSION = 5;
    public static final String DATABASE_NAME = "malmo.db";


    public SQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Location.createTable(db);
        Audio.createTable(db);
        Document.createTable(db);
        LocalFile.createTable(db);
        AudioAds.createTable(db);
        Language.createTable(db);
        AudioType.createTable(db);
        PurchaseResult.createTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Table update here ~

        if (oldVersion < 2) {
            execSQL(db, "ALTER TABLE '" + AudioType.TABLE_NAME + "' ADD COLUMN android_product_id VARCHAR(100);");
            execSQL(db, "ALTER TABLE '" + AudioType.TABLE_NAME + "' ADD COLUMN ios_product_id VARCHAR(100);");
            execSQL(db, "ALTER TABLE '" + Audio.TABLE_NAME + "' ADD COLUMN type VARCHAR(255);");
            PurchaseResult.createTable(db);
        }

        if (oldVersion < 3) {
            execSQL(db, "ALTER TABLE '" + AudioType.TABLE_NAME + "' ADD COLUMN geo_json TEXT;");
        }

        if (oldVersion < 4) {
            execSQL(db, "ALTER TABLE '" + AudioType.TABLE_NAME + "' ADD COLUMN hide_list INTEGER DEFAULT 0;");
        }

        if (oldVersion < 5) {
            execSQL(db, "ALTER TABLE '" + Location.TABLE_NAME + "' ADD COLUMN visibility INTEGER DEFAULT 1;");
        }
    }

    private void execSQL(SQLiteDatabase db, String sql) {
        try {
            db.execSQL(sql);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static int getInt(Cursor c, String column) {
        int value = 0;
        try {
            int ind = c.getColumnIndex(column);
            if (ind > -1)
                value = c.getInt(ind);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static long getLong(Cursor c, String column) {
        long value = 0;
        try {
            int ind = c.getColumnIndex(column);
            if (ind > -1)
                value = c.getLong(ind);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static double getDouble(Cursor c, String column) {
        double value = 0;
        try {
            int ind = c.getColumnIndex(column);
            if (ind > -1)
                value = c.getDouble(ind);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return value;
    }

    public static String getString(Cursor c, String column) {
        String value = "";
        try {
            int ind = c.getColumnIndex(column);
            if (ind > -1)
                value = c.getString(ind);
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (StringHelper.isEmpty(value)) {
            value = "";
        }
        return value;
    }
}
