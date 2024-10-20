package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class SlideToSeekPatch {
    private static final Boolean SLIDE_TO_SEEK_DISABLED = !Settings.SLIDE_TO_SEEK.get();

    public static boolean isSlideToSeekDisabled(boolean isDisabled) {
        if (!isDisabled) return isDisabled;

        return SLIDE_TO_SEEK_DISABLED;
    }
}
