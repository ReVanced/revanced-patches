package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideTimestampPatch {
    public static boolean hideTimestamp() {
        return SettingsEnum.HIDE_TIMESTAMP.getBoolean();
    }
}
