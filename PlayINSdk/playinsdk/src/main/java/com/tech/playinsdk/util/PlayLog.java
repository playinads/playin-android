package com.tech.playinsdk.util;

import android.util.Log;

import com.tech.playinsdk.BuildConfig;

public class PlayLog {

    private static final String sTAG = "PLAYIN";

    public static boolean DEBUG = true;

    public static void v(String message) {
        if (DEBUG) {
            Log.v(sTAG, message);
        }
    }

    public static void i(String message) {
        Log.i(sTAG, message);
    }

    public static void d(String message) {
        Log.d(sTAG, message);
    }

    public static void e(String message) {
        Log.e(sTAG, message);
    }
}

