package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class OpenLinksExternallyPatch {
    /**
     * Override 'android.support.customtabs.action.CustomTabsService',
     * in order to open links in the default browser. This is done by returning an empty string,
     * for the service that handles custom tabs in the Android support library
     * which opens links in the default service instead.
     *
     * @param original The original custom tabs service.
     * @return The new, default service to open links with or the original service.
     */
    public static String enableExternalBrowser(String original) {
        if (SettingsEnum.EXTERNAL_BROWSER.getBoolean()) original = "";
        return original;
    }
}
