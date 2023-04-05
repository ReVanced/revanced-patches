package app.revanced.integrations.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import java.util.Objects;

import app.revanced.integrations.utils.ReVancedUtils;

public enum SharedPrefCategory {
    YOUTUBE("youtube"),
    RETURN_YOUTUBE_DISLIKE("ryd"),
    SPONSOR_BLOCK("sponsor-block"),
    REVANCED_PREFS("revanced_prefs");

    @NonNull
    public final String prefName;
    @NonNull
    public final SharedPreferences preferences;

    SharedPrefCategory(@NonNull String prefName) {
        this.prefName = Objects.requireNonNull(prefName);
        preferences = Objects.requireNonNull(ReVancedUtils.getContext()).getSharedPreferences(prefName, Context.MODE_PRIVATE);
    }

    public void saveString(@NonNull String key, @NonNull String value) {
        Objects.requireNonNull(value);
        preferences.edit().putString(key, value).apply();
    }

    public void saveBoolean(@NonNull String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public void saveInt(@NonNull String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public void saveLong(@NonNull String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public void saveFloat(@NonNull String key, float value) {
        preferences.edit().putFloat(key, value).apply();
    }


    @NonNull
    public String getString(@NonNull String key, @NonNull String _default) {
        Objects.requireNonNull(_default);
        return preferences.getString(key, _default);
    }

    public boolean getBoolean(@NonNull String key, boolean _default) {
        return preferences.getBoolean(key, _default);
    }

    // region Hack, required for PreferencesFragments to function correctly.  unknown why required

    @NonNull
    public Integer getInt(@NonNull String key, @NonNull Integer _default) {
        try {
            return Integer.valueOf(preferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return preferences.getInt(key, _default);
        }
    }

    @NonNull
    public Long getLong(@NonNull String key, @NonNull Long _default) {
        try {
            return Long.valueOf(preferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return preferences.getLong(key, _default);
        }
    }

    @NonNull
    public Float getFloat(@NonNull String key, @NonNull Float _default) {
        try {
            return Float.valueOf(preferences.getString(key, _default.toString()));
        } catch (ClassCastException ex) {
            return preferences.getFloat(key, _default);
        }
    }

    // endregion


    @NonNull
    @Override
    public String toString() {
        return prefName;
    }
}
