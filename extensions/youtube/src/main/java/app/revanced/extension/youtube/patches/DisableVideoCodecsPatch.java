package app.revanced.extension.youtube.patches;

import android.view.Display;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisableVideoCodecsPatch {

    /**
     * Injection point.
     */
    public static int[] disableHdrVideo(Display.HdrCapabilities capabilities) {
        return Settings.DISABLE_HDR_VIDEO.get()
                ? new int[0]
                : capabilities.getSupportedHdrTypes();
    }

    /**
     * Injection point.
     */
    public static boolean disableVP9Codec() {
        return !Settings.DISABLE_VP9_CODEC.get();
    }
}

