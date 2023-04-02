package app.revanced.integrations.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Objects;

public class SharedPrefHelper {
    public static void saveString(SharedPrefNames prefName, String key, String value) {
        getPreferences(prefName).edit().putString(key, value).apply();
    }

    public static void saveBoolean(SharedPrefNames prefName, String key, boolean value) {
        getPreferences(prefName).edit().putBoolean(key, value).apply();
    }

    public static void saveFloat(SharedPrefNames prefName, String key, float value) {
        getPreferences(prefName).edit().putFloat(key, value).apply();
    }

    public static void saveInt(SharedPrefNames prefName, String key, int value) {
        getPreferences(prefName).edit().putInt(key, value).apply();
    }

    public static void saveLong(SharedPrefNames prefName, String key, long value) {
        getPreferences(prefName).edit().putLong(key, value).apply();
    }

    public static String getString(SharedPrefNames prefName, String key, String _default) {
        return getPreferences(prefName).getString(key, _default);
    }

    public static boolean getBoolean(SharedPrefNames prefName, String key, boolean _default) {
        return getPreferences(prefName).getBoolean(key, _default);
    }

    // region Hack, unknown why required

    public static Long getLong(SharedPrefNames prefName, String key, long _default) {
        SharedPreferences sharedPreferences = getPreferences(prefName);
        try {
            return Long.valueOf(sharedPreferences.getString(key, _default + ""));
        } catch (ClassCastException ex) {
            return sharedPreferences.getLong(key, _default);
        }
    }

    public static Float getFloat(SharedPrefNames prefName, String key, float _default) {
        SharedPreferences sharedPreferences = getPreferences(prefName);
        try {
            return Float.valueOf(sharedPreferences.getString(key, _default + ""));
        } catch (ClassCastException ex) {
            return sharedPreferences.getFloat(key, _default);
        }
    }

    public static Integer getInt(SharedPrefNames prefName, String key, int _default) {
        SharedPreferences sharedPreferences = getPreferences(prefName);
        try {
            return Integer.valueOf(sharedPreferences.getString(key, _default + ""));
        } catch (ClassCastException ex) {
            return sharedPreferences.getInt(key, _default);
        }
    }

    // endregion

    public static SharedPreferences getPreferences(SharedPrefNames name) {
        return Objects.requireNonNull(ReVancedUtils.getContext()).getSharedPreferences(name.getName(), Context.MODE_PRIVATE);
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
