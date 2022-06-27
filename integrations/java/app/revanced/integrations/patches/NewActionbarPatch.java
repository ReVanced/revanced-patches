package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class NewActionbarPatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1Jg2WK9wwSABCiIcqclzhedy3J3RCf3Hn/view?usp=sharing for where it needs to be used.
    public static boolean getNewActionBar() {
        return SettingsEnum.USE_NEW_ACTIONBAR_BOOLEAN.getBoolean();
    }

}
