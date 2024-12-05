package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

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
