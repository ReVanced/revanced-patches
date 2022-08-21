package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class NewActionbarPatch {

    //Used by app.revanced.patches.youtube.layout.widesearchbar.patch.WideSearchbarPatch
    public static boolean getNewActionBar() {
        return SettingsEnum.WIDE_SEARCHBAR.getBoolean(); // TODO: maybe this has to be inverted
    }

}
