package app.revanced.twitch.utils;

import android.util.Log;
import android.widget.Toast;

import app.revanced.twitch.settings.SettingsEnum;

public class LogHelper {

    public static String getCallOrigin()
    {
        try {
            final StackTraceElement elem = new Throwable().getStackTrace()[/* depth */ 2];
            final String fullName = elem.getClassName();
            return fullName.substring(fullName.lastIndexOf('.') + 1) + "/" + elem.getMethodName();
        }
        catch (Exception ex) {
            return "<unknown>";
        }
    }

    public static final String TAG = "revanced";

    public static void debug(String message, Object... args) {
        Log.d(TAG, getCallOrigin() + ": " + String.format(message, args));
    }

    public static void info(String message, Object... args) {
        Log.i(TAG, getCallOrigin() + ": " + String.format(message, args));
    }

    public static void error(String message, Object... args) {
        String msg = getCallOrigin() + ": " + String.format(message, args);
        showDebugToast(msg);
        Log.e(TAG, msg);
    }

    public static void printException(String message, Throwable ex) {
        String msg = getCallOrigin() + ": " + message;
        showDebugToast(msg + " (" + ex.getClass().getSimpleName() + ")");
        Log.e(TAG, msg, ex);
    }

    private static void showDebugToast(String msg) {
        if(SettingsEnum.DEBUG_MODE.getBoolean()) {
            ReVancedUtils.ifContextAttached((c) -> Toast.makeText(c, msg, Toast.LENGTH_SHORT).show());
        }
    }
}
