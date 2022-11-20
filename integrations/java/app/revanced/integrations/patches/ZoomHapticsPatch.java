package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class ZoomHapticsPatch {
    public static boolean shouldVibrate() {
        return !SettingsEnum.DISABLE_ZOOM_HAPTICS.getBoolean();
    }
}
