package app.revanced.extension.spotify.misc.privacy;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {
    private static final String TRACKING_PARAMETER_REGEX = "(?:\\?|&)si=.+?($|&)";

    public static String sanitizeUrl(String url) {
        return url.replaceFirst(TRACKING_PARAMETER_REGEX, "$1");
    }
}