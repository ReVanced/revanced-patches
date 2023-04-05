package app.revanced.tiktok.settings;

import static java.lang.Boolean.FALSE;
import static java.lang.Boolean.TRUE;
import static app.revanced.tiktok.settings.SettingsEnum.ReturnType.BOOLEAN;
import static app.revanced.tiktok.settings.SettingsEnum.ReturnType.STRING;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import app.revanced.tiktok.utils.LogHelper;
import app.revanced.tiktok.utils.ReVancedUtils;

public enum SettingsEnum {
    //TikTok Settings
    TIK_DEBUG("tik_debug", BOOLEAN, FALSE), // must be first value, otherwise logging during loading will not work
    TIK_REMOVE_ADS("tik_remove_ads", BOOLEAN, TRUE, true),
    TIK_HIDE_LIVE("tik_hide_live", BOOLEAN, FALSE, true),
    TIK_DOWN_PATH("tik_down_path", STRING, "DCIM/TikTok"),
    TIK_DOWN_WATERMARK("tik_down_watermark", BOOLEAN, TRUE),
    TIK_SIMSPOOF("tik_simspoof", BOOLEAN, TRUE, true),
    TIK_SIMSPOOF_ISO("tik_simspoof_iso", STRING, "us"),
    TIK_SIMSPOOF_MCCMNC("tik_simspoof_mccmnc", STRING, "310160"),
    TIK_SIMSPOOF_OP_NAME("tik_simspoof_op_name", STRING, "T-Mobile");

    static {
        loadAllSettings();
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
    SettingsEnum(@NonNull String path, @NonNull ReturnType returnType, @NonNull Object defaultValue,
                 @NonNull SharedPrefCategory prefName, boolean rebootApp) {
        this.path = path;
        this.returnType = returnType;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
        this.rebootApp = rebootApp;
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

    public enum ReturnType {
        BOOLEAN,
        INTEGER,
        LONG,
        FLOAT,
        STRING,
    }
}
