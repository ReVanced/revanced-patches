package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ZoomHapticsPatch {
    public static boolean shouldVibrate() {
        return !Settings.DISABLE_ZOOM_HAPTICS.get();
    }
}
