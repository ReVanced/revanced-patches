package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideTimestampPatch {
    public static boolean hideTimestamp() {
        return Settings.HIDE_TIMESTAMP.get();
    }
}
