package com.trailsi.malmo.android.common.utils;

import java.util.Random;

public class StringHelper {

    public static final String BLANK = "";
    public static final String NULL = "null";

    private static final char[] symbols;
    private static final char[] symbolsNumber;
    private static final Random mRandom = new Random();
    private static char[] mChBuffer = null;

    static {
        char ch;
        StringBuilder tmp = new StringBuilder();
        for (ch = '0'; ch <= '9'; ++ch)
            tmp.append(ch);
        for (ch = 'A'; ch <= 'Z'; ++ch)
            tmp.append(ch);
        for (ch = 'a'; ch <= 'z'; ++ch)
            tmp.append(ch);
        symbols = tmp.toString().toCharArray();

        StringBuilder tmpNum = new StringBuilder();
        for (ch = '0'; ch <= '9'; ++ch)
            tmpNum.append(ch);
        symbolsNumber = tmpNum.toString().toCharArray();
    }

    public static boolean isEmpty(String str) {
        return str == null || BLANK.equals(str);
    }

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static String getNotNullString(String str) {
        if (StringHelper.isEmpty(str)) return "";
        else return str;
    }
}
