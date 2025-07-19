package app.revanced.twitter.patches.links;

public final class ChangeLinkSharingDomainPatch {
    private static final String DOMAIN_NAME = "https://fxtwitter.com";
    private static final String LINK_FORMAT = "%s/%s/status/%s";

    private static String getShareDomain() {
        return DOMAIN_NAME;
    }

    public static String formatResourceLink(Object... formatArgs) {
        String username = (String) formatArgs[0];
        String tweetId = (String) formatArgs[1];
        return String.format(LINK_FORMAT, getShareDomain(), username, tweetId);
    }

    public static String formatLink(long tweetId, String username) {
        return String.format(LINK_FORMAT, getShareDomain(), username, tweetId);
    }
}
