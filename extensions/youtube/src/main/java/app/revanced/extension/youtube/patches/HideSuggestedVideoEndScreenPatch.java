package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideSuggestedVideoEndScreenPatch {
    /**
     * Injection point.
     */
    public static boolean hideSuggestedVideoEndScreen() {
        return Settings.HIDE_SUGGESTED_VIDEO_END_SCREEN.get();
    }
}
