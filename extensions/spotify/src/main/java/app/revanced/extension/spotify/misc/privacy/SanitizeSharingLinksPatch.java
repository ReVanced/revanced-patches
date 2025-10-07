package app.revanced.extension.spotify.misc.privacy;

import app.revanced.extension.shared.privacy.LinkSanitizer;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {

    private static final LinkSanitizer sanitizer = new LinkSanitizer(
            "si", // Share tracking parameter.
            "utm_source" // Share source, such as "copy-link".
    );

    /**
     * Injection point.
     */
    public static String sanitizeSharingLink(String url) {
        return sanitizer.sanitizeUrlString(url);
    }
}
