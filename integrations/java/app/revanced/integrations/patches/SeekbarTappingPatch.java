package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class SeekbarTappingPatch {
    public static boolean seekbarTappingEnabled() {
        return SettingsEnum.SEEKBAR_TAPPING.getBoolean();
    }
}
