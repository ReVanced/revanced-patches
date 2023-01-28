package app.revanced.twitch.utils;

import android.util.Log;
import android.widget.Toast;

import app.revanced.twitch.settings.SettingsEnum;

/**
 * TODO: replace this with the higher performance logging code from {@link app.revanced.integrations.utils.LogHelper}
 */
public class LogHelper {

    /**
     * TODO: replace this with {@link app.revanced.integrations.utils.LogHelper.LogMessage#findOuterClassSimpleName()}
     */
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
            ReVancedUtils.toast(msg, false);
        }
    }
}
