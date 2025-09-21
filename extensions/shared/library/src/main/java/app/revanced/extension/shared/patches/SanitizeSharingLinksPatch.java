package app.revanced.extension.shared.patches;

import app.revanced.extension.shared.settings.BaseSettings;

/**
 * YouTube and YouTube Music.
 */
@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {
    private static final String NEW_TRACKING_PARAMETER_REGEX = ".si=.+";
    private static final String OLD_TRACKING_PARAMETER_REGEX = ".feature=.+";

    /**
     * Injection point.
     */
    public static String sanitize(String url) {
        if (BaseSettings.SANITIZE_SHARED_LINKS.get()) {
            url = url
                    .replaceAll(NEW_TRACKING_PARAMETER_REGEX, "")
                    .replaceAll(OLD_TRACKING_PARAMETER_REGEX, "");
        }

        if (BaseSettings.REPLACE_MUSIC_LINKS_WITH_YOUTUBE.get()) {
            url = url.replace("music.youtube.com", "youtube.com");
        }

        return url;
    }
}
