package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class TabletMiniPlayerOverridePatch {

    public static boolean getTabletMiniPlayerOverride(boolean original) {
        if (Settings.USE_TABLET_MINIPLAYER.get())
            return true;
        return original;
    }
}
