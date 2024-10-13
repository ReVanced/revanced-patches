package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class SeekbarTappingPatch {
    public static boolean seekbarTappingEnabled() {
        return Settings.SEEKBAR_TAPPING.get();
    }
}
