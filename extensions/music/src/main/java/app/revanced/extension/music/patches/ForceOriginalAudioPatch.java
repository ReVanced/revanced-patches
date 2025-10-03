package app.revanced.extension.music.patches;

import app.revanced.extension.music.settings.Settings;

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
