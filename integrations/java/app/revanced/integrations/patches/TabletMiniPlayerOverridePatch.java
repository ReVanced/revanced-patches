package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class TabletMiniPlayerOverridePatch {

    public static boolean getTabletMiniPlayerOverride(boolean original) {
        if (SettingsEnum.USE_TABLET_MINIPLAYER.getBoolean())
            return true;
        return original;
    }
}
