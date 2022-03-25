package fi.razerman.youtube.Helpers;

import android.os.Build;
import android.util.Log;
import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Objects;

import fi.razerman.youtube.XGlobals;

/* loaded from: classes6.dex */
public class XThemeHelpers {
    static String TAG = "XTheme";
    static int themeValue;

    public static void setTheme(int value) {
        themeValue = value;
        if (XGlobals.debug) {
            String str = TAG;
            Log.d(str, "Theme value: " + themeValue);
        }
    }

    public static void setTheme(Object value) {
        themeValue = ((Enum) value).ordinal();
        if (XGlobals.debug) {
            String str = TAG;
            Log.d(str, "Theme value: " + themeValue);
        }
    }

    public static boolean isDarkTheme() {
        return themeValue == 1;
    }

    public static boolean isNightDarkMode() {
        return (Objects.requireNonNull(YouTubeTikTokRoot_Application.getAppContext()).getResources().getConfiguration().uiMode & 48) == 32;
    }

    public static boolean isAbovePie() {
        return Build.VERSION.SDK_INT > 28;
    }
}
