package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class SeekbarTappingPatch {
    public static boolean seekbarTappingEnabled() {
        return Settings.SEEKBAR_TAPPING.get();
    }
}
