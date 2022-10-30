package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideWatchinVRPatch {
    //Used by app.revanced.patches.youtube.layout.watchinvr.patch.HideWatchinVRPatch
    public static boolean hideWatchinVR() {
        return SettingsEnum.HIDE_WATCH_IN_VR.getBoolean();
    }
}
