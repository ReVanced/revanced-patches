package app.revanced.extension.shared.privacy;

import android.net.Uri;
import app.revanced.extension.shared.Logger;

import java.util.List;

public abstract class SanitizeSharingLinkPatch {
    protected abstract List<String> getParametersToRemove();

    public String sanitizeUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            Uri.Builder builder = uri.buildUpon().clearQuery();

            for (String paramName : uri.getQueryParameterNames()) {
                if (!getParametersToRemove().contains(paramName)) {
                    for (String value : uri.getQueryParameters(paramName)) {
                        builder.appendQueryParameter(paramName, value);
                    }
                }
            }

            String sanitizedUrl = builder.build().toString();
            Logger.printInfo(() -> "Sanitized url: " + url + " to: " + sanitizedUrl);
            return sanitizedUrl;
        } catch (Exception ex) {
            Logger.printException(() -> "sanitizeUrl failure with " + url, ex);
            return url;
        }
    }
}
