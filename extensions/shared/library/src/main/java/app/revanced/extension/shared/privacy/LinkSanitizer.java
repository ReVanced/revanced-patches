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

    public String sanitizeUrlString(String url) {
        try {
            return sanitizeUri(Uri.parse(url)).toString();
        } catch (Exception ex) {
            Logger.printException(() -> "sanitizeUrlString failure: " + url, ex);
            return url;
        }
    }

    public Uri sanitizeUri(Uri uri) {
        try {
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

            Uri sanitizedUrl = builder.build();
            Logger.printInfo(() -> "Sanitized url: " + uri + " to: " + sanitizedUrl);

            return sanitizedUrl;
        } catch (Exception ex) {
            Logger.printException(() -> "sanitizeUri failure: " + uri, ex);
            return uri;
        }
    }
}
