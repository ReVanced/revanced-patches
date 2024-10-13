package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideFloatingMicrophoneButtonPatch {
    public static boolean hideFloatingMicrophoneButton(final boolean original) {
        return Settings.HIDE_FLOATING_MICROPHONE_BUTTON.get() || original;
    }
}
