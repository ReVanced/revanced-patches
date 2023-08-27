package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class EnableTabletLayoutPatch {
    public static boolean enableTabletLayout() {
        return SettingsEnum.TABLET_LAYOUT.getBoolean();
    }
}
