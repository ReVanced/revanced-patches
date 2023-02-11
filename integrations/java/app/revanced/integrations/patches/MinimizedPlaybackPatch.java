package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.shared.PlayerType;

public class MinimizedPlaybackPatch {

    public static boolean isNotPlayingShorts(boolean isPipEnabled) {
        return !PlayerType.getCurrent().isNoneOrHidden() && isPipEnabled;
    }

    public static boolean isMinimizedPlaybackEnabled() {
        return SettingsEnum.ENABLE_MINIMIZED_PLAYBACK.getBoolean();
    }

}
