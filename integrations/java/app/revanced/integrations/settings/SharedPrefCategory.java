package app.revanced.integrations.settings;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.integrations.utils.ReVancedUtils;

/**
 * Shared categories, and helper methods.
 *
 * The various save methods store numbers as Strings,
 * which is required if using {@link android.preference.PreferenceFragment}.
 *
 * If saved numbers will not be used with a preference fragment,
 * then store the primitive numbers using {@link #preferences}.
 */
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

    private void saveObjectAsString(@NonNull String key, @Nullable Object value) {
        preferences.edit().putString(key, (value == null ? null : value.toString())).apply();
    }

    public void saveBoolean(@NonNull String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    /**
     * @param value a NULL parameter removes the value from the preferences
     */
    public void saveIntegerString(@NonNull String key, @Nullable Integer value) {
        saveObjectAsString(key, value);
    }

    /**
     * @param value a NULL parameter removes the value from the preferences
     */
    public void saveLongString(@NonNull String key, @Nullable Long value) {
        saveObjectAsString(key, value);
    }

    /**
     * @param value a NULL parameter removes the value from the preferences
     */
    public void saveFloatString(@NonNull String key, @Nullable Float value) {
        saveObjectAsString(key, value);
    }

    /**
     * @param value a NULL parameter removes the value from the preferences
     */
    public void saveString(@NonNull String key, @Nullable String value) {
        saveObjectAsString(key, value);
    }

    @NonNull
    public String getString(@NonNull String key, @NonNull String _default) {
        Objects.requireNonNull(_default);
        return preferences.getString(key, _default);
    }


    public boolean getBoolean(@NonNull String key, boolean _default) {
        return preferences.getBoolean(key, _default);
    }

    @NonNull
    public Integer getIntegerString(@NonNull String key, @NonNull Integer _default) {
        try {
            String value = preferences.getString(key, null);
            if (value != null) {
                return Integer.valueOf(value);
            }
            return _default;
        } catch (ClassCastException ex) {
            return preferences.getInt(key, _default); // old data, previously stored as primitive
        }
    }

    @NonNull
    public Long getLongString(@NonNull String key, @NonNull Long _default) {
        try {
            String value = preferences.getString(key, null);
            if (value != null) {
                return Long.valueOf(value);
            }
            return _default;
        } catch (ClassCastException ex) {
            return preferences.getLong(key, _default);
        }
    }

    @NonNull
    public Float getFloatString(@NonNull String key, @NonNull Float _default) {
        try {
            String value = preferences.getString(key, null);
            if (value != null) {
                return Float.valueOf(value);
            }
            return _default;
        } catch (ClassCastException ex) {
            return preferences.getFloat(key, _default);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return prefName;
    }
}
