package com.trailsi.kvinnokarta.android.common.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class TimeHelper {

    private static final String FORMAT_ISO8601Z = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String MM_SS = "mm:ss";

    public static String getCurrentTime() {
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_ISO8601Z, Locale.US);
        Calendar now = Calendar.getInstance();
        Date date = new Date(now.getTimeInMillis());
        return df.format(date);
    }

    public static String getTimeFromMilliseconds(int milliseconds) {
        int total = milliseconds / 1000;
        int minutes = total / 60;
        int seconds = total % 60;
        return String.format(Locale.US, "%02d:%02d", minutes, seconds);
    }

    public static int getTimeFromString(String position) {
        String[] fields = position.split(":");
        if (fields.length != 2) {
            return 0;
        }
        if (!StringHelper.isNumeric(fields[0]) || !StringHelper.isNumeric(fields[1])) {
            return 0;
        }
        return Integer.parseInt(fields[0]) * 60 + Integer.parseInt(fields[1]);
    }
}
