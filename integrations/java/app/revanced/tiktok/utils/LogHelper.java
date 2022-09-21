package app.revanced.tiktok.utils;

import android.util.Log;

import app.revanced.tiktok.settings.SettingsEnum;

public class LogHelper {

    //ToDo: Get Calling classname using Reflection

    public static void debug(Class clazz, String message) {
        if (SettingsEnum.TIK_DEBUG.getBoolean()) {
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
