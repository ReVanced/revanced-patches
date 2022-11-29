package app.revanced.twitch.patches;


import app.revanced.twitch.settings.SettingsEnum;

public class DebugModePatch {
    public static boolean isDebugModeEnabled() {
        return SettingsEnum.DEBUG_MODE.getBoolean();
    }
}
