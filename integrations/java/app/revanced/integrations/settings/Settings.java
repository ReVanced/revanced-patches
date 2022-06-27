package app.revanced.integrations.settings;

public class Settings {

    //Methods not used in latest Vanced source code, can be useful for future patches
    /*

    private static Object AutoRepeatClass;

    public static boolean getVerticalZoomToFit(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        } else if (!verticalZoomToFit.booleanValue()) {
            return original;
        } else {
            LogHelper.debug("Settings", "getVerticalZoomToFit: Enabled");
            return true;
        }
    }

    public static int getMinimizedVideo(int original) {
        int preferredType = SettingsEnum.PREFERRED_MINIMIZED_VIDEO_PREVIEW_INTEGER.getInt();
        if (preferredType == -2) {
            return original;
        }
        if (preferredType == 0 || preferredType == 1) {
            return preferredType;
        }
        return original;
    }


    public static boolean getThemeStatus() {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning false!");
            return false;
        } else if (!isDarkApp.booleanValue()) {
            return false;
        } else {
            LogHelper.debug("Settings", "getThemeStatus: Is themed");
            return true;
        }
    }

    public static boolean accessibilitySeek(boolean original) {
        ReadSettings();
        if (!settingsInitialized.booleanValue()) {
            LogHelper.printException("Settings", "Context is null, returning " + original + "!");
            return original;
        }
        Boolean seek = Boolean.valueOf(original);
        if (accessibilitySeek.booleanValue()) {
            seek = true;
        }
        LogHelper.debug("Settings", "accessibilitySeek: " + seek);
        return seek.booleanValue();
    }

    public static void setOldLayout(SharedPreferences sharedPreferences, String config, long timeStamp) {
        if (!SettingsEnum.OLD_LAYOUT_XFILE_ENABLED_BOOLEAN.getBoolean()) {
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group", config).putLong("com.google.android.libraries.youtube.innertube.cold_stored_timestamp", timeStamp).apply();
            LogHelper.debug("Settings", "setOldLayout: true");
            return;
        }

        if (sharedPreferences.contains("com.google.android.libraries.youtube.innertube.cold_config_group")) {
            sharedPreferences.edit().putString("com.google.android.libraries.youtube.innertube.cold_config_group_backup", sharedPreferences.getString("com.google.android.libraries.youtube.innertube.cold_config_group", null)).remove("com.google.android.libraries.youtube.innertube.cold_config_group").apply();
        }
        LogHelper.debug("Settings", "setOldLayout: false");
    }
    */
}
