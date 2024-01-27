package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class OpenLinksExternallyPatch {
    /**
     * Return the intent to open links with. If empty, the link will be opened with the default browser.
     *
     * @param originalIntent The original intent to open links with.
     * @return The intent to open links with. Empty means the link will be opened with the default browser.
     */
    public static String getIntent(String originalIntent) {
        if (Settings.EXTERNAL_BROWSER.get()) return "";

        return originalIntent;
    }
}
