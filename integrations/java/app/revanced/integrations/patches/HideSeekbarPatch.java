package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideSeekbarPatch {
    public static boolean hideSeekbar() {
        return SettingsEnum.HIDE_SEEKBAR.getBoolean();
    }
}
