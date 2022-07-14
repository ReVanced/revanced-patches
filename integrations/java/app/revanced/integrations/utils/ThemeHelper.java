package app.revanced.integrations.utils;

/* loaded from: classes6.dex */
public class ThemeHelper {
    private static int themeValue;

    public static void setTheme(int value) {
        themeValue = value;
        LogHelper.debug(ThemeHelper.class, "Theme value: " + themeValue);
    }

    public static void setTheme(Object value) {
        themeValue = ((Enum) value).ordinal();
        LogHelper.debug(ThemeHelper.class, "Theme value: " + themeValue);
    }

    public static boolean isDarkTheme() {
        return themeValue == 1;
    }

}
