package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class OldStyleQualityPatch {

    //Used by app.revanced.patches.youtube.layout.oldqualitylayout.patch.OldQualityLayoutPatch
    public static boolean useOldStyleQualitySettings() {
        return SettingsEnum.OLD_STYLE_QUALITY_SETTINGS_BOOLEAN.getBoolean();
    }
}
