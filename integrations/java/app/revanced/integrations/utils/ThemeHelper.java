package app.revanced.integrations.utils;

public class ThemeHelper {
    private static int themeValue;

    public static void setTheme(Object value) {
        final int newOrdinalValue = ((Enum) value).ordinal();
        if (themeValue != newOrdinalValue) {
            themeValue = newOrdinalValue;
            LogHelper.printDebug(() -> "Theme value: " + themeValue);
        }
    }

    public static boolean isDarkTheme() {
        return themeValue == 1;
    }

}
