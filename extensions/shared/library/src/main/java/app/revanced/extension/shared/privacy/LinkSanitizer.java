package app.revanced.extension.shared.privacy;

import android.net.Uri;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import app.revanced.extension.shared.Logger;

/**
 * Strips away specific parameters from URLs.
 */
public class LinkSanitizer {

    private final Collection<String> parametersToRemove;

    public LinkSanitizer(String ... parametersToRemove) {
        final int parameterCount = parametersToRemove.length;

        // List is faster if only checking a few parameters.
        this.parametersToRemove = parameterCount > 4
                ? Set.of(parametersToRemove)
                : List.of(parametersToRemove);
    }

    public String sanitizeURLString(String url) {
        try {
            return sanitizeURI(Uri.parse(url)).toString();
        } catch (Exception ex) {
            Logger.printException(() -> "sanitizeURLString failure: " + url, ex);
            return url;
        }
    }

    public Uri sanitizeURI(Uri uri) {
        try {
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
                // Opening YouTube share sheet 'other' option passes the video title as a URI.
                // Checking !uri.isHierarchical() works for all cases, except if the
                // video title starts with / and then it's hierarchical but still an invalid URI.
                Logger.printDebug(() -> "Ignoring URI: " + uri);
                return uri;
            }

            Uri.Builder builder = uri.buildUpon().clearQuery();

            if (!parametersToRemove.isEmpty()) {
                for (String paramName : uri.getQueryParameterNames()) {
                    if (!parametersToRemove.contains(paramName)) {
                        for (String value : uri.getQueryParameters(paramName)) {
                            builder.appendQueryParameter(paramName, value);
                        }
                    }
                }
            }

            Uri sanitizedURL = builder.build();
            Logger.printInfo(() -> "Sanitized URL: " + uri + " to: " + sanitizedURL);

            return sanitizedURL;
        } catch (Exception ex) {
            Logger.printException(() -> "sanitizeURI failure: " + uri, ex);
            return uri;
        }
    }
}
