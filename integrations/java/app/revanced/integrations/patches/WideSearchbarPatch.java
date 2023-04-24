package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public final class WideSearchbarPatch {
    public static boolean enableWideSearchbar() {
        return SettingsEnum.WIDE_SEARCHBAR.getBoolean();
    }
}
