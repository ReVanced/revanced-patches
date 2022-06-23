package app.revanced.integrations.utils;

import android.util.Log;

import app.revanced.integrations.settings.Settings;
import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    public static void debug(String tag, String message) {
        if (SettingsEnum.DEBUG_BOOLEAN.getBoolean()) {
            Log.d(tag, message);
        }
    }

    public static void printException(String tag, String message, Throwable ex) {
        Log.e(tag, message, ex);
    }

    public static void printException(String tag, String message) {
        Log.e(tag, message);
    }

    public static void info(String tag, String message) {
        Log.i(tag, message);
    }

    public static void info(String message) {
        info("ReVanced", message);
    }
}
