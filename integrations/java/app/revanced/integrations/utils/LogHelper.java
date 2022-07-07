package app.revanced.integrations.utils;

import android.util.Log;

import app.revanced.integrations.settings.Settings;
import app.revanced.integrations.settings.SettingsEnum;

public class LogHelper {

    //ToDo: Get Calling classname using Reflection

    public static void debug(Class clazz, String message) {
        if (SettingsEnum.DEBUG_BOOLEAN.getBoolean()) {
            Log.d("ReVanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
        }
    }

    public static void printException(Class clazz, String message, Throwable ex) {
        Log.e("ReVanced: " + (clazz != null ? clazz.getSimpleName() : ""), message, ex);
    }

    public static void printException(Class clazz, String message) {
        Log.e("ReVanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
    }

    public static void info(Class clazz, String message) {
        Log.i("ReVanced: " + (clazz != null ? clazz.getSimpleName() : ""), message);
    }
}
