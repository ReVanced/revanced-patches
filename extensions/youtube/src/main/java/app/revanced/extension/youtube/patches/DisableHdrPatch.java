package app.revanced.extension.youtube.patches;

import android.view.Display;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableHdrPatch {

    /**
     * Injection point.
     */
    public static int[] disableHdrVideo(Display.HdrCapabilities capabilities) {
        return Settings.DISABLE_HDR_VIDEO.get()
                ? new int[0]
                : capabilities.getSupportedHdrTypes();
    }
}

