package app.revanced.twitter.patches.links;

@SuppressWarnings("unused")
public final class ChangeLinkSharingDomainPatch {
    private static final String LINK_FORMAT = "https://%s/%s/status/%s";

    /**
     * Method is modified during patching.  Do not change.
     */
    private static String getShareDomain() {
        return "";
    }

    // TODO remove this once changeLinkSharingDomainResourcePatch is restored
    /**
     * Injection point.
     */
    public static String formatResourceLink(Object... formatArgs) {
        String username = (String) formatArgs[0];
        String tweetId = (String) formatArgs[1];
        return String.format(LINK_FORMAT, getShareDomain(), username, tweetId);
    }

    /**
     * Injection point.
     */
    public static String formatLink(long tweetId, String username) {
        return String.format(LINK_FORMAT, getShareDomain(), username, tweetId);
    }
}
