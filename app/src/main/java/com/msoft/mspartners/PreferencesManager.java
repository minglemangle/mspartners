package com.msoft.mspartners;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferencesManager {
    private static final String TAG = PreferencesManager.class.getSimpleName();

    private static final String PREFERENCES_NAME = "pref";
    private static final String DEFAULT_VALUE_STRING = "";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
    }

    /**
     * Get string
     * @param context Context
     * @param key Key
     * @return
     */
    public static String getString(Context context, String key) {
        SharedPreferences prefs = getPreferences(context);
        String value = prefs.getString(key, DEFAULT_VALUE_STRING);
        return value;
    }

    /**
     * Save string
     *
     * @param context Context
     * @param key Key
     * @param value String value
     */
    public static void setString(Context context, String key, String value) {
        SharedPreferences prefs = getPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(key, value);
        editor.commit();
    }
}
