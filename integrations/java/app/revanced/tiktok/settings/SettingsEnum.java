package app.revanced.tiktok.settings;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.revanced.tiktok.utils.LogHelper;
import app.revanced.tiktok.utils.ReVancedUtils;

import java.util.HashMap;
import java.util.Map;

import static app.revanced.tiktok.settings.SettingsEnum.ReturnType.BOOLEAN;
import static app.revanced.tiktok.settings.SettingsEnum.ReturnType.STRING;
import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;

public enum SettingsEnum {
    DEBUG("debug", BOOLEAN, FALSE), // Must be first value, otherwise logging during loading will not work.
    REMOVE_ADS("remove_ads", BOOLEAN, TRUE, true),
    HIDE_LIVE("hide_live", BOOLEAN, FALSE, true),
    HIDE_STORY("hide_story", BOOLEAN, FALSE, true),
    HIDE_IMAGE("hide_image", BOOLEAN, FALSE, true),
    MIN_MAX_VIEWS("min_max_views", STRING, "0-" + Long.MAX_VALUE, true),
    MIN_MAX_LIKES("min_max_likes", STRING, "0-" + Long.MAX_VALUE, true),
    DOWNLOAD_PATH("down_path", STRING, "DCIM/TikTok"),
    DOWNLOAD_WATERMARK("down_watermark", BOOLEAN, TRUE),
    SIM_SPOOF("simspoof", BOOLEAN, TRUE, true),
    SIM_SPOOF_ISO("simspoof_iso", STRING, "us"),
    SIMSPOOF_MCCMNC("simspoof_mccmnc", STRING, "310160"),
    SIMSPOOF_OP_NAME("simspoof_op_name", STRING, "T-Mobile");

    private static final Map<String, SettingsEnum> pathToSetting = new HashMap<>(2 * values().length);

    static {
        loadAllSettings();
        for (SettingsEnum setting : values()) {
            pathToSetting.put(setting.path, setting);
        }
    }

    @NonNull
    public final String path;
    @NonNull
    public final Object defaultValue;
    @NonNull
    public final SharedPrefCategory sharedPref;
    @NonNull
    public final ReturnType returnType;
    /**
     * If the app should be rebooted, if this setting is changed
     */
    public final boolean rebootApp;

    private Object value;

    SettingsEnum(String path, ReturnType returnType, Object defaultValue) {
        this(path, returnType, defaultValue, SharedPrefCategory.TIKTOK_PREFS, false);
    }

    SettingsEnum(String path, ReturnType returnType, Object defaultValue, boolean rebootApp) {
        this(path, returnType, defaultValue, SharedPrefCategory.TIKTOK_PREFS, rebootApp);
    }

    SettingsEnum(@NonNull String path, @NonNull ReturnType returnType, @NonNull Object defaultValue, @NonNull SharedPrefCategory prefName, boolean rebootApp) {
        this.path = path;
        this.returnType = returnType;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
        this.rebootApp = rebootApp;
    }

    @Nullable
    public static SettingsEnum getSettingsFromPath(@NonNull String str) {
        return pathToSetting.get(str);
    }

    private static void loadAllSettings() {
        try {
            Context context = ReVancedUtils.getAppContext();
            if (context == null) {
                Log.e("revanced: SettingsEnum", "Context returned null! Settings NOT initialized");
                return;
            }
            for (SettingsEnum setting : values()) {
                setting.load(context);
            }
        } catch (Exception ex) {
            LogHelper.printException(SettingsEnum.class, "Error during load()!", ex);
        }
    }

    private void load(Context context) {
        switch (returnType) {
            case BOOLEAN:
                value = sharedPref.getBoolean(context, path, (boolean) defaultValue);
                break;
            case INTEGER:
                value = sharedPref.getInt(context, path, (Integer) defaultValue);
                break;
            case LONG:
                value = sharedPref.getLong(context, path, (Long) defaultValue);
                break;
            case FLOAT:
                value = sharedPref.getFloat(context, path, (Float) defaultValue);
                break;
            case STRING:
                value = sharedPref.getString(context, path, (String) defaultValue);
                break;
            default:
                throw new IllegalStateException(name());
        }

        LogHelper.debug(SettingsEnum.class, "Loaded Setting: " + name() + " Value: " + value);
    }

    /**
     * Sets, but does _not_ persistently save the value.
     *
     * This intentionally is a static method, to deter accidental usage
     * when {@link #saveValue(Object)} was intended.
     */
    public static void setValue(SettingsEnum setting, Object newValue) {
        // FIXME: this should validate the parameter matches the return type
        setting.value = newValue;
    }

    public void saveValue(Object newValue) {
        Context context = ReVancedUtils.getAppContext();
        if (context == null) {
            LogHelper.printException(SettingsEnum.class, "Context on SaveValue is null!");
            return;
        }

        if (returnType == BOOLEAN) {
            sharedPref.saveBoolean(context, path, (Boolean) newValue);
        } else {
            sharedPref.saveString(context, path, newValue.toString());
        }
        value = newValue;
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

    /**
     * @return the value of this setting as as generic object type.
     */
    @NonNull
    public Object getObjectValue() {
        return value;
    }

    public enum ReturnType {
        BOOLEAN, INTEGER, LONG, FLOAT, STRING,
    }
}
