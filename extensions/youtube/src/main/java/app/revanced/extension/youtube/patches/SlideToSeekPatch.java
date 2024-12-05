package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class SlideToSeekPatch {
    private static final boolean SLIDE_TO_SEEK_DISABLED = !Settings.SLIDE_TO_SEEK.get();

    public static boolean isSlideToSeekDisabled(boolean isDisabled) {
        if (!isDisabled) return false;

        return SLIDE_TO_SEEK_DISABLED;
    }
}
