package app.revanced.twitch.settings;

import android.content.Context;
import android.content.SharedPreferences;

import app.revanced.twitch.utils.LogHelper;
import app.revanced.twitch.utils.ReVancedUtils;

public enum SettingsEnum {
    /* Ads */
    BLOCK_VIDEO_ADS("revanced_block_video_ads", true, ReturnType.BOOLEAN),
    BLOCK_AUDIO_ADS("revanced_block_audio_ads", true, ReturnType.BOOLEAN),

    /* Chat */
    SHOW_DELETED_MESSAGES("revanced_show_deleted_messages", "cross-out", ReturnType.STRING),

    /* Misc */
    DEBUG_MODE("revanced_debug_mode", false, ReturnType.BOOLEAN, true);

    public static final String REVANCED_PREFS = "revanced_prefs";

    private final String path;
    private final Object defaultValue;
    private final ReturnType returnType;
    private final boolean rebootApp;

    private Object value = null;

    SettingsEnum(String path, Object defaultValue, ReturnType returnType) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.returnType = returnType;
        this.rebootApp = false;
    }

    SettingsEnum(String path, Object defaultValue, ReturnType returnType, Boolean rebootApp) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.returnType = returnType;
        this.rebootApp = rebootApp;
    }

    static {
        load();
    }

    private static void load() {
        ReVancedUtils.ifContextAttached((context -> {
            try {
                SharedPreferences prefs = context.getSharedPreferences(REVANCED_PREFS, Context.MODE_PRIVATE);
                for (SettingsEnum setting : values()) {
                    Object value = setting.getDefaultValue();

                    try {
                        switch (setting.getReturnType()) {
                            case BOOLEAN:
                                value = prefs.getBoolean(setting.getPath(), (boolean)setting.getDefaultValue());
                                break;
                            // Numbers are implicitly converted from strings
                            case FLOAT:
                            case LONG:
                            case INTEGER:
                            case STRING:
                                value = prefs.getString(setting.getPath(), setting.getDefaultValue() + "");
                                break;
                            default:
                                LogHelper.error("Setting '%s' does not have a valid type", setting.name());
                                break;
                        }
                    }
                    catch (ClassCastException ex) {
                        LogHelper.printException("Failed to read value", ex);
                    }

                    setting.setValue(value);
                    LogHelper.debug("Loaded setting '%s' with value %s", setting.name(), value);
                }
            } catch (Throwable th) {
                LogHelper.printException("Failed to load settings", th);
            }
        }));
    }

    public void setValue(Object newValue) {
        // Implicitly convert strings to numbers depending on the ResultType
        switch (returnType) {
            case FLOAT:
                value = Float.valueOf(newValue + "");
                break;
            case LONG:
                value = Long.valueOf(newValue + "");
                break;
            case INTEGER:
                value = Integer.valueOf(newValue + "");
                break;
            default:
                value = newValue;
                break;
        }
    }

    public void saveValue(Object newValue) {
        ReVancedUtils.ifContextAttached((context) -> {
            SharedPreferences prefs = context.getSharedPreferences(REVANCED_PREFS, Context.MODE_PRIVATE);
            if (returnType == ReturnType.BOOLEAN) {
                prefs.edit().putBoolean(path, (Boolean)newValue).apply();
            } else {
                prefs.edit().putString(path, newValue + "").apply();
            }
            value = newValue;
        });
    }

    public int getInt() {
        return (int) value;
    }

    public String getString() {
        return (String) value;
    }

    public boolean getBoolean() {
        return (Boolean) value;
    }

    public Long getLong() {
        return (Long) value;
    }

    public Float getFloat() {
        return (Float) value;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }

    public String getPath() {
        return path;
    }

    public ReturnType getReturnType() {
        return returnType;
    }

    public boolean shouldRebootOnChange() {
        return rebootApp;
    }
}
