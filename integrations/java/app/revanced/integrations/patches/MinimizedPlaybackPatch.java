package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class MinimizedPlaybackPatch {

    public static boolean isMinimizedPlaybackEnabled() {
        return SettingsEnum.ENABLE_MINIMIZED_PLAYBACK.getBoolean();
    }

}
