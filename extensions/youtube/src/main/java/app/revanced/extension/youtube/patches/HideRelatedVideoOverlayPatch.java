package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideRelatedVideoOverlayPatch {
    /**
     * Injection point.
     */
    public static boolean hideRelatedVideoOverlay() {
        return Settings.HIDE_RELATED_VIDEOS_OVERLAY.get();
    }
}
