package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class TabletLayoutPatch {

    private static final boolean TABLET_LAYOUT_ENABLED = Settings.TABLET_LAYOUT.get();

    /**
     * Injection point.
     */
    public static boolean getTabletLayoutEnabled() {
        return TABLET_LAYOUT_ENABLED;
    }
}
