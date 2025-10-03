package app.revanced.extension.instagram.misc.privacy;

import app.revanced.extension.shared.privacy.LinkSanitizer;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {
    private static final LinkSanitizer sanitizer = new LinkSanitizer("igsh");

    /**
     * Injection point.
     */
    public static String sanitizeSharingLink(String url) {
        return sanitizer.sanitizeUrlString(url);
    }
}
