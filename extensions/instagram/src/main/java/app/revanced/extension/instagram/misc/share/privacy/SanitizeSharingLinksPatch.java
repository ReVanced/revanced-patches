package app.revanced.extension.instagram.misc.share.privacy;

import app.revanced.extension.shared.privacy.SanitizeSharingLinkPatch;

import java.util.List;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch extends SanitizeSharingLinkPatch {
    private static final SanitizeSharingLinksPatch INSTANCE = new SanitizeSharingLinksPatch();

    /**
     * Parameters that are considered undesirable and should be stripped away.
     */
    @Override
    protected List<String> getParametersToRemove() {
        return List.of("igsh");
    }

    /**
     * Injection point.
     */
    public static String sanitizeSharingLink(String url) {
        return INSTANCE.sanitizeUrl(url);
    }
}
