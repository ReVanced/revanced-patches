package app.revanced.extension.spotify.misc.privacy;

import android.net.Uri;
import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {
    public static String sanitizeUrl(String url) {
        try {
            Uri uri = Uri.parse(url);
            Uri.Builder builder = uri.buildUpon().clearQuery();

            for (String paramName : uri.getQueryParameterNames()) {
                if (!paramName.equals("si")) {
                    for (String value : uri.getQueryParameters(paramName)) {
                        builder.appendQueryParameter(paramName, value);
                    }
                }
            }

            return builder.build().toString();
        } catch (Exception ex) {
            Logger.printException(() -> "sanitizeUrl failure", ex);

            return url;
        }
    }
}