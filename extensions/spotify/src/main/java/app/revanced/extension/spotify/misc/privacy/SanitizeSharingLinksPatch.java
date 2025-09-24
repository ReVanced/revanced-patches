package app.revanced.extension.spotify.misc.privacy;

import java.util.List;

import app.revanced.extension.shared.privacy.SanitizeSharingLinkPatch;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch extends SanitizeSharingLinkPatch {
    private static final SanitizeSharingLinksPatch INSTANCE = new SanitizeSharingLinksPatch();

    /**
     * Parameters that are considered undesirable and should be stripped away.
     */
    @Override
    protected List<String> getParametersToRemove() {
        return List.of(
                "si", // Share tracking parameter.
                "utm_source" // Share source, such as "copy-link".
        );
    }

    /**
     * Injection point.
     */
    public static String sanitizeSharingLink(String url) {
        return INSTANCE.sanitizeUrl(url);
    }
}
