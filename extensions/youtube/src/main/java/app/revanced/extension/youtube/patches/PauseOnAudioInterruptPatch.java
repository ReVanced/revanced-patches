package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class PauseOnAudioInterruptPatch {

    private static final int AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK = -3;
    private static final int AUDIOFOCUS_LOSS_TRANSIENT = -2;

    /**
     * Injection point for AudioFocusRequest builder.
     * Returns true if audio ducking should be disabled (willPauseWhenDucked = true).
     */
    public static boolean shouldPauseOnAudioInterrupt() {
        return Settings.PAUSE_ON_AUDIO_INTERRUPT.get();
    }

    /**
     * Injection point for onAudioFocusChange callback.
     * Converts AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK to AUDIOFOCUS_LOSS_TRANSIENT
     * when the setting is enabled, causing YouTube to pause instead of ducking.
     */
    public static int overrideAudioFocusChange(int focusChange) {
        if (Settings.PAUSE_ON_AUDIO_INTERRUPT.get() && focusChange == AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK) {
            return AUDIOFOCUS_LOSS_TRANSIENT;
        }
        return focusChange;
    }
}
