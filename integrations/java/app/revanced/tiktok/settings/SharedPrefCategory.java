package app.revanced.tiktok.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

public enum SharedPrefCategory {
    TIKTOK_PREFS("tiktok_revanced");

    @NonNull
    public final String prefName;

    SharedPrefCategory(@NonNull String prefName) {
        this.prefName = prefName;
    }

    @NonNull
    @Override
    public String toString() {
        return prefName;
    }

    private SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public void saveBoolean(Context context, String key, boolean value) {
        getPreferences(context).edit().putBoolean(key, value).apply();
    }

    public void saveString(Context context, String key, String value) {
        getPreferences(context).edit().putString(key, value).apply();
    }

    public boolean getBoolean(Context context, String key, boolean _default) {
        return getPreferences(context).getBoolean(key, _default);
    }

    public Integer getInt(Context context, String key, Integer _default) {
        SharedPreferences sharedPreferences = getPreferences(context);
        try {
            return Integer.valueOf(sharedPreferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return sharedPreferences.getInt(key, _default);
        }
    }

    public Long getLong(Context context, String key, Long _default) {
        SharedPreferences sharedPreferences = getPreferences(context);
        try {
            return Long.valueOf(sharedPreferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return sharedPreferences.getLong(key, _default);
        }
    }

    public Float getFloat(Context context, String key, Float _default) {
        SharedPreferences sharedPreferences = getPreferences(context);
        try {
            return Float.valueOf(sharedPreferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return sharedPreferences.getFloat(key, _default);
        }
    }

    public String getString(Context context, String key, String _default) {
        return getPreferences(context).getString(key, _default);
    }

}
