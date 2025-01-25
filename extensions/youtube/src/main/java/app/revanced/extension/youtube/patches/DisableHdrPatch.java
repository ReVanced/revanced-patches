package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableHdrPatch {

    /**
     * Injection point.
     */
    public static boolean disableHDRVideo() {
        return !Settings.DISABLE_HDR_VIDEO.get();
    }
}

