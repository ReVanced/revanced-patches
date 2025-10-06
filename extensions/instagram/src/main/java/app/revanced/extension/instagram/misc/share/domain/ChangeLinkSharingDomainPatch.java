package app.revanced.extension.instagram.misc.share.domain;

import android.net.Uri;
import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public final class ChangeLinkSharingDomainPatch {
    /**
     * This method will be modified by the patch, in order for it to return the share domain name.
     * In the smali code there were no free registers.
     */
    private static String getCustomShareDomain() {
        return "";
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
