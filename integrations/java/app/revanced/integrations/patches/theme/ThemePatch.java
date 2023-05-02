package app.revanced.integrations.patches.theme;

import android.graphics.Color;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.ReVancedUtils;

public final class ThemePatch {
    public static final int DEFAULT_SEEKBAR_COLOR = 0xffff0000;

    private static void resetSeekbarColor() {
        ReVancedUtils.showToastShort("Invalid seekbar color value. Using default value.");
        SettingsEnum.SEEKBAR_COLOR.saveValue(Integer.toHexString(DEFAULT_SEEKBAR_COLOR));
    }

    public static int getSeekbarColorValue() {
        try {
            return Color.parseColor(SettingsEnum.SEEKBAR_COLOR.getString());
        } catch (IllegalArgumentException exception) {
            resetSeekbarColor();
            return DEFAULT_SEEKBAR_COLOR;
        }
    }
}
