package app.revanced.tiktok.clearmode;

import app.revanced.tiktok.settings.SettingsEnum;

public class RememberClearModePatch {
    public static boolean getClearModeState() {
        return SettingsEnum.CLEAR_MODE.getBoolean();
    }
    public static void rememberClearModeState(boolean newState) {
        SettingsEnum.CLEAR_MODE.saveValue(newState);
    }
}
