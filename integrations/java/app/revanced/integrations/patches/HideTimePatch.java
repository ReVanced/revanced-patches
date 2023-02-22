package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class HideTimePatch {
    public static boolean hideTime() {
        return SettingsEnum.HIDE_TIME.getBoolean();
    }
}
