package app.revanced.extension.twitch.patches;

import app.revanced.extension.twitch.settings.Settings;

@SuppressWarnings("unused")
public class DebugModePatch {
    public static boolean isDebugModeEnabled() {
        return Settings.TWITCH_DEBUG_MODE.get();
    }
}
