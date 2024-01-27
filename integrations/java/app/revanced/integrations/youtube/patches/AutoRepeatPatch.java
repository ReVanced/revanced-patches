package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class AutoRepeatPatch {
    //Used by app.revanced.patches.youtube.layout.autorepeat.patch.AutoRepeatPatch
    public static boolean shouldAutoRepeat() {
        return Settings.AUTO_REPEAT.get();
    }
}
