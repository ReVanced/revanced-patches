package app.revanced.reddit.patches;

import app.revanced.integrations.utils.LogHelper;

import java.net.MalformedURLException;
import java.net.URL;

public final class SanitizeUrlQueryPatch {
    /**
     * Strip query parameters from a given URL string.
     *
     * @param urlString URL string to strip query parameters from.
     * @return URL string without query parameters if possible, otherwise the original string.
     */
    public static String stripQueryParameters(final String urlString) {
        try {
            final var url = new URL(urlString);

            return url.getProtocol() + "://" + url.getHost() + url.getPath();
        } catch (MalformedURLException e) {
            LogHelper.printException(() -> "Can not parse URL", e);
            return urlString;
        }
    }
}
