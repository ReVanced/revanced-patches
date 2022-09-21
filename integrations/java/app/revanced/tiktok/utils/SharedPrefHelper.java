package app.revanced.tiktok.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefHelper {
    public static void saveString(Context context, SharedPrefNames prefName, String key, String value) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        sharedPreferences.edit().putString(key, value).apply();
    }

    public static void saveBoolean(Context context, SharedPrefNames prefName, String key, Boolean value) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        sharedPreferences.edit().putBoolean(key, value).apply();
    }

    public static String getString(Context context, SharedPrefNames prefName, String key, String _default) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        return (sharedPreferences.getString(key, _default));
    }

    public static Boolean getBoolean(Context context, SharedPrefNames prefName, String key, Boolean _default) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        return (sharedPreferences.getBoolean(key, _default));
    }

    public static Long getLong(Context context, SharedPrefNames prefName, String key, Long _default) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        try {
            return Long.valueOf(sharedPreferences.getString(key, _default + ""));
        } catch (ClassCastException ex) {
            return sharedPreferences.getLong(key, _default);
        }
    }

    public static Float getFloat(Context context, SharedPrefNames prefName, String key, Float _default) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        try {
            return Float.valueOf(sharedPreferences.getString(key, _default + ""));
        } catch (ClassCastException ex) {
            return sharedPreferences.getFloat(key, _default);
        }
    }

    public static Integer getInt(Context context, SharedPrefNames prefName, String key, Integer _default) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        try {
            return Integer.valueOf(sharedPreferences.getString(key, _default + ""));
        } catch (ClassCastException ex) {
            return sharedPreferences.getInt(key, _default);
        }
    }

    public static SharedPreferences getPreferences(Context context, SharedPrefNames name) {
        if (context == null) return null;
        return context.getSharedPreferences(name.getName(), Context.MODE_PRIVATE);
    }

    public static SharedPreferences getPreferences(Context context, String name) {
        if (context == null) return null;
        return context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    public enum SharedPrefNames {
        TIKTOK_PREFS("tiktok_revanced");

        private final String name;

        SharedPrefNames(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        @Override
        public String toString() {
            return name;
        }
    }
}
