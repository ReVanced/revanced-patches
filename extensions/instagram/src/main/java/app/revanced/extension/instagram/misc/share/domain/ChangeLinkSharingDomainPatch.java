package app.revanced.extension.instagram.misc.share.domain;

import android.net.Uri;
import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public final class ChangeLinkSharingDomainPatch {

    private static String getCustomShareDomain() {
        // Method is modified during patching.
        throw new IllegalStateException();
    }

    /**
     * Injection point.
     */
    public static String setCustomShareDomain(String url) {
        try {
            Uri uri = Uri.parse(url);
            Uri.Builder builder = uri
                    .buildUpon()
                    .authority(getCustomShareDomain())
                    .clearQuery();

            String patchedUrl = builder.build().toString();
            Logger.printInfo(() -> "Domain change from : " + url + " to: " + patchedUrl);
            return patchedUrl;
        } catch (Exception ex) {
            Logger.printException(() -> "setCustomShareDomain failure with " + url, ex);
            return url;
        }
    }
}
