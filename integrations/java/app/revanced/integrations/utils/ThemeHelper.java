package app.revanced.integrations.utils;

import android.os.Build;

import com.google.android.apps.youtube.app.YouTubeTikTokRoot_Application;

import java.util.Objects;

import app.revanced.integrations.utils.LogHelper;

/* loaded from: classes6.dex */
public class ThemeHelper {
    static int themeValue;

    public static void setTheme(int value) {
        themeValue = value;
        LogHelper.debug("XTheme", "Theme value: " + themeValue);
    }

    public static void setTheme(Object value) {
        themeValue = ((Enum) value).ordinal();
        LogHelper.debug("XTheme", "Theme value: " + themeValue);
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
