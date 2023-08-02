package app.revanced.tiktok.utils;

import android.util.Log;

import app.revanced.tiktok.settings.SettingsEnum;

/**
 * TODO: replace this with the higher performance logging code from {@link app.revanced.integrations.utils.LogHelper}
 */
public class LogHelper {

    public static void debug(Class clazz, String message) {
        if (SettingsEnum.DEBUG.getBoolean()) {
            Log.d("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
        }
    }

    public static void printException(Class clazz, String message, Throwable ex) {
        Log.e("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message, ex);
    }

    public static void printException(Class clazz, String message) {
        Log.e("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
    }

    public static void info(Class clazz, String message) {
        Log.i("revanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
    }
}
