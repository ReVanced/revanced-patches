package app.revanced.integrations.utils;

public class ThemeHelper {
    private static int themeValue;

    public static void setTheme(int value) {
        themeValue = value;
        LogHelper.printDebug(() -> "Theme value: " + themeValue);
    }

    public static void setTheme(Object value) {
        themeValue = ((Enum) value).ordinal();
        LogHelper.printDebug(() -> "Theme value: " + themeValue);
    }

    public static boolean isDarkTheme() {
        return themeValue == 1;
    }

}
