package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.Settings;

@SuppressWarnings("unused")
public class DebugModePatch {
    public static boolean isDebugModeEnabled() {
        return Settings.TWITCH_DEBUG_MODE.get();
    }
}
