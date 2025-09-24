package app.revanced.extension.instagram.misc.privacy;

import app.revanced.extension.shared.privacy.SanitizeSharingLink;

import java.util.List;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {
    /**
     * Parameters that are considered undesirable and should be stripped away.
     */
    private static final List<String> SHARE_PARAMETERS_TO_REMOVE = List.of(
            "igsh"
    );

    /**
     * Injection point.
     */
    public static String sanitizeSharingLink(String url) {
        return SanitizeSharingLink.sanitizeUrl(url, SHARE_PARAMETERS_TO_REMOVE);
    }
}
