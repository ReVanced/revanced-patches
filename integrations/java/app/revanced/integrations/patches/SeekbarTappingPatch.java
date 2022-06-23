package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class SeekbarTappingPatch {

    //Used by app.revanced.patches.youtube.interaction.seekbar.patch.EnableSeekbarTappingPatch
    public static boolean isTapSeekingEnabled() {
        return SettingsEnum.TAP_SEEKING_ENABLED_BOOLEAN.getBoolean();
    }

}
