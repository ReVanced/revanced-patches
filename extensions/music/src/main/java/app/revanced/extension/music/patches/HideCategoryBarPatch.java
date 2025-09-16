package app.revanced.extension.music.patches;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideCategoryBarPatch {

    /**
     * Injection point
     */
    public static boolean hideCategoryBar() {
        return Settings.HIDE_CATEGORY_BAR.get();
    }
}
