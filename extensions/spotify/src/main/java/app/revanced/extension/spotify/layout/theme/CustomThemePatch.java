package app.revanced.extension.spotify.layout.theme;

import android.graphics.Color;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class CustomThemePatch {

    /**
     * Injection point.
     */
    public static long getColorLong(String colorString) {
        try {
            if (colorString.startsWith("#")) {
                return Color.parseColor(colorString);
            }
            return Utils.getResourceColor(colorString);
        } catch (Exception ex) {
            Logger.printException(() -> "Invalid custom color: " + colorString, ex);
            return Color.BLACK;
        }
    }
}
