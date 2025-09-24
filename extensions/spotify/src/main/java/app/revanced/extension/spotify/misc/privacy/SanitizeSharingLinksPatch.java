package app.revanced.extension.spotify.misc.privacy;

import java.util.List;

import app.revanced.extension.shared.privacy.SanitizeSharingLink;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {

    /**
     * Parameters that are considered undesirable and should be stripped away.
     */
    private static final List<String> SHARE_PARAMETERS_TO_REMOVE = List.of(
            "si", // Share tracking parameter.
            "utm_source" // Share source, such as "copy-link".
    );

    /**
     * Injection point.
     */
    public static String sanitizeSharingLink(String url) {
        return SanitizeSharingLink.sanitizeUrl(url, SHARE_PARAMETERS_TO_REMOVE);
    }
}
