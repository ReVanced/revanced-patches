package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HideFloatingMicrophoneButtonPatch {
    public static boolean hideFloatingMicrophoneButton(final boolean original) {
        return Settings.HIDE_FLOATING_MICROPHONE_BUTTON.get() || original;
    }
}
