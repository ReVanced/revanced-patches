package app.revanced.extension.instagram.misc.links;

import android.net.Uri;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

@SuppressWarnings("unused")
public final class OpenLinksExternallyPatch {

    /**
     * Injection point.
     */
    public static boolean openExternally(String url) {
        try {
            // The "url" parameter to this function will be of the form.
            // https://l.instagram.com/?u=<actual url>&e=<tracking id>
            String actualUrl = Uri.parse(url).getQueryParameter("u");
            if (actualUrl != null) {
                Utils.openLink(actualUrl);
                return true;
            }

        } catch (Exception ex) {
            Logger.printException(() -> "openExternally failure", ex);
        }

        return false;
    }
}
