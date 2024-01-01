package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class SlideToSeekPatch {
    public static boolean isSlideToSeekDisabled() {
        return !Settings.SLIDE_TO_SEEK.get();
    }
}
