package app.revanced.integrations.youtube;

import android.app.Activity;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;

public class ThemeHelper {
    private static int themeValue;

    public static void setTheme(Object value) {
        final int newOrdinalValue = ((Enum) value).ordinal();
        if (themeValue != newOrdinalValue) {
            themeValue = newOrdinalValue;
            Logger.printDebug(() -> "Theme value: " + newOrdinalValue);
        }
    }

    public static boolean isDarkTheme() {
        return themeValue == 1;
    }

    public static void setActivityTheme(Activity activity) {
        final var theme = isDarkTheme()
                ? "Theme.YouTube.Settings.Dark"
                : "Theme.YouTube.Settings";
        activity.setTheme(Utils.getResourceIdentifier(theme, "style"));
    }

}
