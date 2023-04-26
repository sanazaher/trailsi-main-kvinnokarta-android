package com.trailsi.malmo.android.common.cache;


import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONObject;

import java.util.Iterator;

public class CachedData {

    private static SharedPreferences preferences;

    private static final Object sLock = new Object();
    private static final String PREF_NAME = "Malmo";

    public static final String kLanguage = "language";
    public static final String kAudioType = "audio_type";
    public static final String kPlaceInfoDownloaded = "place_info_downloaded";
    public static final String kPlaceMapImage = "place_map";
    public static final String kPlaceUUID = "place_uuid";
    public static final String kPlaceUseGPS = "place_use_gps";
    public static final String kPlaceTopLeftLatitude = "place_top_left_latitude";
    public static final String kPlaceTopLeftLongitude = "place_top_left_longitude";
    public static final String kPlaceTopRightLatitude = "place_top_right_latitude";
    public static final String kPlaceTopRightLongitude = "place_top_right_longitude";
    public static final String kPlaceBottomRightLatitude = "place_bottom_right_latitude";
    public static final String kPlaceBottomRightLongitude = "place_bottom_right_longitude";
    public static final String kPlaceBottomLeftLatitude = "place_bottom_left_latitude";
    public static final String kPlaceBottomLeftLongitude = "place_bottom_left_longitude";
    public static final String kPlaceInfoTitle = "place_info_title";
    public static final String kPlaceInfoDescription = "place_info_description";
    public static final String kPlaceInfoImage = "place_info_image";
    public static final String kDownloadedAudioCounts = "downloaded_audio_counts";

    public synchronized static void init(Context context) {
        synchronized (sLock) {
            preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        }
    }

    public synchronized static void clearAll() {
        synchronized (sLock) {
            preferences.edit().clear().apply();
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        synchronized (sLock) {
            return preferences.getBoolean(key, defaultValue);
        }
    }

    public static void setBoolean(String key, boolean value) {
        synchronized (sLock) {
            preferences.edit().putBoolean(key, value).apply();
        }
    }

    public static String getString(String key, String defaultValue) {
        synchronized (sLock) {
            return preferences.getString(key, defaultValue);
        }
    }

    public static void setString(String key, String value) {
        if (value == null) value = "";
        synchronized (sLock) {
            preferences.edit().putString(key, value).apply();
        }
    }

    public static int getInt(String key, int defaultValue) {
        synchronized (sLock) {
            return preferences.getInt(key, defaultValue);
        }
    }

    public static void setInt(String key, int value) {
        synchronized (sLock) {
            preferences.edit().putInt(key, value).apply();
        }
    }

    public static long getLong(String key, long defaultValue) {
        synchronized (sLock) {
            return preferences.getLong(key, defaultValue);
        }
    }

    public static void setLong(String key, long value) {
        synchronized (sLock) {
            preferences.edit().putLong(key, value).apply();
        }
    }

    public static double getDouble(String key, double defaultValue) {
        synchronized (sLock) {
            return (double) preferences.getFloat(key, (float) defaultValue);
        }
    }

    public static void setDouble(String key, double value) {
        synchronized (sLock) {
            preferences.edit().putFloat(key, (float) value).apply();
        }
    }

    public static int getCountForCurrentType() {
        int usedCount = 0;
        try {
            String countJson = getString(kDownloadedAudioCounts, "");
            String currentType = getString(kAudioType, "");
            JSONObject jsonObject = new JSONObject(countJson);
            Iterator<?> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                if (currentType.equals(key)) {
                    usedCount = jsonObject.getInt(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return usedCount;
    }

    public static void increaseCountForType() {
        JSONObject jsonObject = new JSONObject();
        try {
            String countJson = getString(kDownloadedAudioCounts, "");
            jsonObject = new JSONObject(countJson);
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            int newValue = 0;
            String currentType = getString(kAudioType, "");
            Iterator<?> iterator = jsonObject.keys();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                int value = jsonObject.getInt(key);
                if (currentType.equals(key)) {
                    newValue = value;
                }
            }
            jsonObject.put(currentType, newValue + 1);
            setString(kDownloadedAudioCounts, jsonObject.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

