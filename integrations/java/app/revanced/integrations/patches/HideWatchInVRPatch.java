package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideWatchInVRPatch {
    public static boolean hideWatchInVR() {
        return SettingsEnum.HIDE_WATCH_IN_VR.getBoolean();
    }
}
