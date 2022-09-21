package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideTimeAndSeekbarPatch {
    //Used by app.revanced.patches.youtube.layout.hidetimeandseekbar.patch.HideTimeAndSeekbarPatch
    public static boolean hideTimeAndSeekbar() {
        return SettingsEnum.HIDE_TIME_AND_SEEKBAR.getBoolean();
    }
}
