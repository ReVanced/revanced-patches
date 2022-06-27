package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class AutoRepeatPatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1hLl71Mm3oAtgTjNvsYZi3CUutCPx2gjS/view?usp=sharing for where it needs to be used.
    public static boolean shouldAutoRepeat() {
        return SettingsEnum.PREFERRED_AUTO_REPEAT_BOOLEAN.getBoolean();
    }
}
