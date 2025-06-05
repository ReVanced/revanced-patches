package app.revanced.extension.spotify.misc.privacy;

import android.net.Uri;

import java.util.List;

import app.revanced.extension.shared.Logger;

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
    public static String sanitizeUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            Uri.Builder builder = uri.buildUpon().clearQuery();

            for (String paramName : uri.getQueryParameterNames()) {
                if (!SHARE_PARAMETERS_TO_REMOVE.contains(paramName)) {
                    for (String value : uri.getQueryParameters(paramName)) {
                        builder.appendQueryParameter(paramName, value);
                    }
                }
            }

            String sanitizedUrl = builder.build().toString();
            Logger.printInfo(() -> "Sanitized url " + url + " to " + sanitizedUrl);
            return sanitizedUrl;
        } catch (Exception ex) {
            Logger.printException(() -> "sanitizeUrl failure with " + url, ex);
            return url;
        }
    }
}
