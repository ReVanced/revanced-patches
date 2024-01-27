package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class EnableTabletLayoutPatch {
    public static boolean enableTabletLayout() {
        return Settings.TABLET_LAYOUT.get();
    }
}
