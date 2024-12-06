package app.revanced.extension.youtube.patches;

import static app.revanced.extension.youtube.settings.Settings.BYPASS_IMAGE_REGION_RESTRICTIONS;

import java.util.regex.Pattern;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class BypassImageRegionRestrictionsPatch {

    private static final boolean BYPASS_IMAGE_REGION_RESTRICTIONS_ENABLED = BYPASS_IMAGE_REGION_RESTRICTIONS.get();

    private static final String REPLACEMENT_IMAGE_DOMAIN = "https://yt4.ggpht.com";

    /**
     * YouTube static images domain.  Includes user and channel avatar images and community post images.
     */
    private static final Pattern YOUTUBE_STATIC_IMAGE_DOMAIN_PATTERN
            = Pattern.compile("^https://(yt3|lh[3-6]|play-lh)\\.(ggpht|googleusercontent)\\.com");

    /**
     * Injection point.  Called off the main thread and by multiple threads at the same time.
     *
     * @param originalUrl Image url for all image urls loaded.
     */
    public static String overrideImageURL(String originalUrl) {
        try {
            if (BYPASS_IMAGE_REGION_RESTRICTIONS_ENABLED) {
                String replacement = YOUTUBE_STATIC_IMAGE_DOMAIN_PATTERN
                        .matcher(originalUrl).replaceFirst(REPLACEMENT_IMAGE_DOMAIN);

                if (Settings.DEBUG.get() && !replacement.equals(originalUrl)) {
                    Logger.printDebug(() -> "Replaced: '" + originalUrl + "' with: '" + replacement + "'");
                }

                return replacement;
            }
        } catch (Exception ex) {
            Logger.printException(() -> "overrideImageURL failure", ex);
        }

        return originalUrl;
    }
}
