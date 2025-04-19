package app.revanced.extension.spotify.misc.privacy;

import android.net.Uri;

@SuppressWarnings("unused")
public final class SanitizeSharingLinksPatch {
    public static String sanitizeUrl(String url) {
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
    }
}