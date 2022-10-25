package app.revanced.integrations.patches;

import android.util.Log;

import app.revanced.integrations.utils.ThemeHelper;

public class LithoThemePatch {
    // color constants used in relation with litho components
    private static final int[] WHITECONSTANTS = {
        -1, // comments chip background
        -394759, // music related results panel background
        -83886081, // video chapters list background
    };

    private static final int[] DARKCONSTANTS = {
        -14606047, // comments chip background
        -15198184, // music related results panel background
        -15790321, // comments chip background (new layout)
        -98492127 // video chapters list background
    };

    // Used by app.revanced.patches.youtube.layout.theme.patch.LithoThemePatch
    public static int applyLithoTheme(int originalValue) {
        var isDarkTheme = ThemeHelper.isDarkTheme();
        
        if ((isDarkTheme && anyEquals(originalValue, DARKCONSTANTS)) || (!isDarkTheme  && anyEquals(originalValue, WHITECONSTANTS)))
                return 0;
        return originalValue;
    }

    private static boolean anyEquals(int value, int... of) {
        for (int v : of) if (value == v) return true;
        return false;
    }
}
