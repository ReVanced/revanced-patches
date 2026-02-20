package app.revanced.extension.tiktok.share;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.privacy.LinkSanitizer;
import app.revanced.extension.shared.settings.BaseSettings;

@SuppressWarnings("unused")
public final class ShareUrlSanitizer {

    private static final LinkSanitizer sanitizer = new LinkSanitizer();

    /**
     * Injection point for setting check.
     */
    public static boolean shouldSanitize() {
        return BaseSettings.SANITIZE_SHARED_LINKS.get();
    }

    /**
     * Injection point for URL sanitization.
     */
    public static String sanitizeShareUrl(final String url) {
        if (url == null || url.isEmpty()) {
            return url;
        }

        String sanitized = sanitizer.sanitizeUrlString(url);
        if (BaseSettings.DEBUG.get() && sanitized != null && !sanitized.equals(url)) {
            Logger.printInfo(() -> "[ReVanced SanitizeShareUrl] "
                    + truncate(url) + " -> " + truncate(sanitized));
        }
        return sanitized;
    }

    private static String truncate(String url) {
        if (url == null) return "null";
        if (url.length() <= 160) return "\"" + url + "\"";
        return "\"" + url.substring(0, 160) + "...\"";
    }
}
