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

        return sanitizer.sanitizeUrlString(url);
    }
}
