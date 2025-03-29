package app.revanced.extension.spotify.layout.theme;

import android.graphics.Color;

import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class CustomThemePatch {
    public static long getColorInt(String colorString) {
        if (colorString.startsWith("#")) {
            return Long.valueOf(Color.parseColor(colorString));
        }
        return Long.valueOf(Utils.getResourceColor(colorString));
    }
}
