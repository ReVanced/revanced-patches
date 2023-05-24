package app.revanced.integrations.utils;

import android.app.Activity;

public class ThemeHelper {
    private static int themeValue;

    public static void setTheme(Object value) {
        final int newOrdinalValue = ((Enum) value).ordinal();
        if (themeValue != newOrdinalValue) {
            themeValue = newOrdinalValue;
            LogHelper.printDebug(() -> "Theme value: " + newOrdinalValue);
        }
    }

    public static boolean isDarkTheme() {
        return themeValue == 1;
    }

    public static void setActivityTheme(Activity activity) {
        final var theme = isDarkTheme()
                ? "Theme.YouTube.Settings.Dark"
                : "Theme.YouTube.Settings";
        activity.setTheme(ReVancedUtils.getResourceIdentifier(theme, "style"));
    }

}
