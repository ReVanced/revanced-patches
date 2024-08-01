package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class SlideToSeekPatch {
    public static boolean isSlideToSeekDisabled() {
        return !Settings.SLIDE_TO_SEEK.get();
    }
}
