package app.revanced.tiktok.settings;

import android.content.Context;
import android.util.Log;

import app.revanced.tiktok.utils.LogHelper;
import app.revanced.tiktok.utils.ReVancedUtils;
import app.revanced.tiktok.utils.SharedPrefHelper;

public enum SettingsEnum {
    //TikTok Settings
    TIK_REMOVE_ADS("tik-remove-ads", true, SharedPrefHelper.SharedPrefNames.TIKTOK_PREFS, ReturnType.BOOLEAN, true),
    TIK_HIDE_LIVE("tik-hide-live", false, SharedPrefHelper.SharedPrefNames.TIKTOK_PREFS, ReturnType.BOOLEAN, true),
    TIK_DOWN_PATH("tik-down-path", "DCIM/TikTok", SharedPrefHelper.SharedPrefNames.TIKTOK_PREFS, ReturnType.STRING),
    TIK_DOWN_WATERMARK("tik-down-watermark", true, SharedPrefHelper.SharedPrefNames.TIKTOK_PREFS, ReturnType.BOOLEAN),
    TIK_DEBUG("tik_debug", false, SharedPrefHelper.SharedPrefNames.TIKTOK_PREFS, ReturnType.BOOLEAN);

    static {
        load();
    }

    private final String path;
    private final Object defaultValue;
    private final SharedPrefHelper.SharedPrefNames sharedPref;
    private final ReturnType returnType;
    private final boolean rebootApp;
    private Object value = null;

    SettingsEnum(String path, Object defaultValue, ReturnType returnType) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = SharedPrefHelper.SharedPrefNames.TIKTOK_PREFS;
        this.returnType = returnType;
        this.rebootApp = false;
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName, ReturnType returnType) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
        this.returnType = returnType;
        this.rebootApp = false;
    }

    SettingsEnum(String path, Object defaultValue, SharedPrefHelper.SharedPrefNames prefName, ReturnType returnType, Boolean rebootApp) {
        this.path = path;
        this.defaultValue = defaultValue;
        this.sharedPref = prefName;
        this.returnType = returnType;
        this.rebootApp = rebootApp;
    }

    private static void load() {
        Context context = ReVancedUtils.getAppContext();
        if (context == null) {
            Log.e("revanced: SettingsEnum", "Context returned null! Setings NOT initialized");
        } else {
            try {
                for (SettingsEnum setting : values()) {
                    Object value = setting.getDefaultValue();

                    //LogHelper is not initialized here
                    Log.d("revanced: SettingsEnum", "Loading Setting: " + setting.name());

                    switch (setting.getReturnType()) {
                        case FLOAT:
                            value = SharedPrefHelper.getFloat(context, setting.sharedPref, setting.getPath(), (float) setting.getDefaultValue());
                            break;
                        case LONG:
                            value = SharedPrefHelper.getLong(context, setting.sharedPref, setting.getPath(), (long) setting.getDefaultValue());
                            break;
                        case BOOLEAN:
                            value = SharedPrefHelper.getBoolean(context, setting.sharedPref, setting.getPath(), (boolean) setting.getDefaultValue());
                            break;
                        case INTEGER:
                            value = SharedPrefHelper.getInt(context, setting.sharedPref, setting.getPath(), (int) setting.getDefaultValue());
                            break;
                        case STRING:
                            value = SharedPrefHelper.getString(context, setting.sharedPref, setting.getPath(), (String) setting.getDefaultValue());
                            break;
                        default:
                            LogHelper.printException(SettingsEnum.class, "Setting does not have a valid Type. Name is: " + setting.name());
                            break;
                    }
                    setting.setValue(value);

                    //LogHelper is not initialized here
                    Log.d("revanced: SettingsEnum", "Loaded Setting: " + setting.name() + " Value: " + value);
                }
            } catch (Throwable th) {
                LogHelper.printException(SettingsEnum.class, "Error during load()!", th);
            }
        }
    }

    public void setValue(Object newValue) {
        this.value = newValue;
    }

    public void saveValue(Object newValue) {
        Context context = ReVancedUtils.getAppContext();
        if (context != null) {
            if (returnType == ReturnType.BOOLEAN) {
                SharedPrefHelper.saveBoolean(context, sharedPref, path, (Boolean) newValue);
            } else {
                SharedPrefHelper.saveString(context, sharedPref, path, newValue + "");
            }
            value = newValue;
        } else {
            LogHelper.printException(SettingsEnum.class, "Context on SaveValue is null!");
        }
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
