package app.revanced.integrations.utils;

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

    public static void saveInt(Context context, SharedPrefNames prefName, String key, Integer value) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        sharedPreferences.edit().putInt(key, value).apply();
    }

    public static void saveLong(Context context, SharedPrefNames prefName, String key, Long value) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        sharedPreferences.edit().putLong(key, value).apply();
    }

    public static void saveFloat(Context context, SharedPrefNames prefName, String key, Float value) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        sharedPreferences.edit().putFloat(key, value).apply();
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
        return sharedPreferences.getLong(key, _default);
    }

    public static Float getFloat(Context context, SharedPrefNames prefName, String key, Float _default) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        return sharedPreferences.getFloat(key, _default);
    }

    public static Integer getInt(Context context, SharedPrefNames prefName, String key, Integer _default) {
        SharedPreferences sharedPreferences = getPreferences(context, prefName);
        return sharedPreferences.getInt(key, _default);
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

        YOUTUBE("youtube"),
        RYD("ryd"),
        SPONSOR_BLOCK("sponsor-block"),
        REVANCED_PREFS("revanced_prefs");

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
