package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class SlideToSeekPatch {
    public static boolean isSlideToSeekDisabled() {
        return !SettingsEnum.SLIDE_TO_SEEK.getBoolean();
    }
}
