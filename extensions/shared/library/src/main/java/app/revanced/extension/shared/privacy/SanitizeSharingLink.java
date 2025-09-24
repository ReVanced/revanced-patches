package app.revanced.extension.shared.privacy;

import android.net.Uri;
import app.revanced.extension.shared.Logger;

import java.util.List;

public class SanitizeSharingLink {
    public static String sanitizeUrl(String url, List<String> shareParameterToRemove) {
        try {
            Uri uri = Uri.parse(url);
            Uri.Builder builder = uri.buildUpon().clearQuery();

            for (String paramName : uri.getQueryParameterNames()) {
                if (!shareParameterToRemove.contains(paramName)) {
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
