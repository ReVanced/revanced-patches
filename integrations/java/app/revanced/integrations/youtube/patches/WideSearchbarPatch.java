package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class WideSearchbarPatch {
    public static boolean enableWideSearchbar() {
        return Settings.WIDE_SEARCHBAR.get();
    }
}
