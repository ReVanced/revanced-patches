package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class OverrideCodecPatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/14d2R-5JF97gOZggoobVEVazPWbORbZVp/view?usp=sharing for where it needs to be used.
    public static boolean isOverrideCodedUsed() {
        return SettingsEnum.CODEC_OVERRIDE_BOOLEAN.getBoolean();
    }

}
