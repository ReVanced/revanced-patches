package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class HideFloatingMicrophoneButtonPatch {
    public static boolean hideFloatingMicrophoneButton(boolean original) {
        return SettingsEnum.HIDE_FLOATING_MICROPHONE_BUTTON.getBoolean() || original;
    }
}
