package app.revanced.twitch.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.twitch.settings.SettingsEnum.ReturnType.BOOLEAN;
import static app.revanced.twitch.settings.SettingsEnum.ReturnType.STRING;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;

import app.revanced.twitch.utils.LogHelper;
import app.revanced.twitch.utils.ReVancedUtils;

public enum SettingsEnum {
    /* Ads */
    BLOCK_VIDEO_ADS("revanced_block_video_ads", BOOLEAN, TRUE),
    BLOCK_AUDIO_ADS("revanced_block_audio_ads", BOOLEAN, TRUE),
    BLOCK_EMBEDDED_ADS("revanced_block_embedded_ads", STRING, "ttv-lol"),

    /* Chat */
    SHOW_DELETED_MESSAGES("revanced_show_deleted_messages", STRING, "cross-out"),

    /* Misc */
    DEBUG_MODE("revanced_debug_mode", BOOLEAN, FALSE, true);

    public static final String REVANCED_PREFS = "revanced_prefs";

    @NonNull
    public final String path;
    @NonNull
    public final ReturnType returnType;
    @NonNull
    public final Object defaultValue;
    /**
     * If the app should be rebooted, if this setting is changed
     */
    public final boolean rebootApp;

    private Object value;

    SettingsEnum(String path, ReturnType returnType, Object defaultValue) {
        this(path, returnType, defaultValue, false);
    }

    SettingsEnum(@NonNull String path, @NonNull ReturnType returnType, @NonNull Object defaultValue, boolean rebootApp) {
        this.path = path;
        this.returnType = returnType;
        this.defaultValue = defaultValue;
        this.rebootApp = rebootApp;
    }

    static {
        loadAllSettings();
    }

    private static void loadAllSettings() {
        ReVancedUtils.ifContextAttached((context -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences(REVANCED_PREFS, Context.MODE_PRIVATE);
                for (SettingsEnum setting : values()) {
                    setting.load(prefs);
                }
            } catch (Exception ex) {
                LogHelper.printException("Failed to load settings", ex);
            }
        }));
    }

    private void load(SharedPreferences prefs) {
        try {
            switch (returnType) {
                case BOOLEAN:
                    setValue(prefs.getBoolean(path, (Boolean) defaultValue));
                    break;
                // Numbers are implicitly converted from strings
                case INTEGER:
                case LONG:
                case FLOAT:
                case STRING:
                    setValue(prefs.getString(path, defaultValue.toString()));
                    break;
                default:
                    throw new IllegalStateException(name());
            }
            LogHelper.debug("Loaded setting '%s' with value %s", name(), value);
        } catch (ClassCastException ex) {
            LogHelper.printException("Failed to read value", ex);
        }
    }

    /**
     * Sets, but does _not_ persistently save the value.
     *
     * This intentionally is a static method, to deter accidental usage
     * when {@link #saveValue(Object)} was intended.
     */
    public static void setValue(SettingsEnum setting, Object newValue) {
        setting.setValue(newValue);
    }

    private void setValue(Object newValue) {
        // Implicitly convert strings to numbers depending on the ResultType
        switch (returnType) {
            case FLOAT:
                value = Float.valueOf(newValue.toString());
                break;
            case LONG:
                value = Long.valueOf(newValue.toString());
                break;
            case INTEGER:
                value = Integer.valueOf(newValue.toString());
                break;
            case BOOLEAN:
            case STRING:
                value = newValue;
                break;
            default:
                throw new IllegalArgumentException(name());
        }
    }

    public void saveValue(Object newValue) {
        ReVancedUtils.ifContextAttached((context) -> {
            SharedPreferences prefs = context.getSharedPreferences(REVANCED_PREFS, Context.MODE_PRIVATE);
            if (returnType == BOOLEAN) {
                prefs.edit().putBoolean(path, (Boolean)newValue).apply();
            } else {
                prefs.edit().putString(path, newValue.toString()).apply();
            }
            setValue(newValue);
        });
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public int getInt() {
        return (Integer) value;
    }

    public long getLong() {
        return (Long) value;
    }

    public float getFloat() {
        return (Float) value;
    }

    public String getString() {
        return (String) value;
    }

    public enum ReturnType {
        BOOLEAN,
        INTEGER,
        LONG,
        FLOAT,
        STRING,
    }
}
