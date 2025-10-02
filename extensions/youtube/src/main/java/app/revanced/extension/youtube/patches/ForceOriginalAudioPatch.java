package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class ForceOriginalAudioPatch {

    /**
     * Injection point.
     */
    public static void setEnabled() {
        app.revanced.extension.shared.patches.ForceOriginalAudioPatch.setEnabled(
                Settings.FORCE_ORIGINAL_AUDIO.get(),
                Settings.SPOOF_VIDEO_STREAMS_CLIENT_TYPE.get()
        );
    }
}
