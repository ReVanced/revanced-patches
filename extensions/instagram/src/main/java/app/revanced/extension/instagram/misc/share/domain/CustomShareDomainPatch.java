package app.revanced.extension.instagram.misc.share.domain;

import android.net.Uri;
import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public final class CustomShareDomainPatch {
    public static String setCustomShareDomain(String url, String customShareDomain) {
        try {
            Uri uri = Uri.parse(url);
            Uri.Builder builder = uri
                    .buildUpon()
                    .authority(customShareDomain)
                    .clearQuery();

            String sanitizedUrl = builder.build().toString();
            Logger.printInfo(() -> "Domain change from : " + url + " to: " + sanitizedUrl);
            return sanitizedUrl;
        } catch (Exception ex) {
            Logger.printException(() -> "setCustomShareDomain failure with " + url + "and custom domain " + customShareDomain, ex);
            return url;
        }
    }
}
