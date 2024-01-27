package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ZoomHapticsPatch {
    public static boolean shouldVibrate() {
        return !Settings.DISABLE_ZOOM_HAPTICS.get();
    }
}
