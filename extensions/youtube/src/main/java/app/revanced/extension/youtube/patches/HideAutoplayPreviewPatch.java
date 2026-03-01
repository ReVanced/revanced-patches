package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideAutoplayPreviewPatch {
    /**
     * Injection point.
     */
    public static boolean hideAutoplayPreview() {
        return Settings.HIDE_AUTOPLAY_PREVIEW.get();
    }
}
