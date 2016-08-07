package com.example.mariostudio.lyricviewdemo.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by MarioStudio on 2016/8/7.
 */

public class PreferenceUtil {

    public static PreferenceUtil preferenceUtil;
    private static SharedPreferences preferences;

    public static final String KEY_TEXT_SIZE = "key.text.size";
    public static final String KEY_LINE_SPACE = "key.line.space";
    public static final String KEY_HIGHLIGHT_COLOR = "key.highlight.color";

    public PreferenceUtil() {

    }

    public static PreferenceUtil getInstance(Context context) {
        if(preferenceUtil == null) {
            preferenceUtil = new PreferenceUtil();
            initPreferenceUtil(context);
        }
        return preferenceUtil;
    }

    private static void initPreferenceUtil(Context context) {
        preferences = context.getSharedPreferences("LyricViewDemo", Context.MODE_PRIVATE);
    }

    public int getInt(String key, int defValue) {
        if(preferences != null) {
            return preferences.getInt(key, defValue);
        }
        return defValue;
    }

    public void putInt(String key, int value) {
        if(preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putInt(key, value);
            editor.commit();
        }
    }

    public String getString(String key, String defValue) {
        if(preferences != null) {
            return preferences.getString(key, defValue);
        }
        return defValue;
    }

    public void putString(String key, String value) {
        if(preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(key, value);
            editor.commit();
        }
    }

    public float getFloat(String key, float defValue) {
        if(preferences != null) {
            return preferences.getFloat(key, defValue);
        }
        return defValue;
    }

    public void putFloat(String key, float value) {
        if(preferences != null) {
            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat(key, value);
            editor.commit();
        }
    }
}
