package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class AutoRepeatPatch {


    //Used by app.revanced.patches.youtube.layout.autorepeat.patch.AutoRepeatPatch
    public static boolean shouldAutoRepeat() {
        return SettingsEnum.PREFERRED_AUTO_REPEAT.getBoolean();
    }
}
