package app.revanced.extension.instagram.misc.links;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import app.revanced.extension.shared.Logger;

public final class OpenLinksExternallyPatch {
    public static boolean openExternally(String url) {
        try {
            // The "url" parameter to this function will be of the form.
            // https://l.instagram.com/?u=<actual url>&e=<tracking id>
            String actualUrl = Uri.parse(url).getQueryParameter("u");
            if (actualUrl != null) {
                app.revanced.extension.shared.patches.OpenLinksExternallyPatch.openLink(actualUrl);
                return true;
            }

        } catch (Exception ex) {
            Logger.printException(() -> "Instagram openExternally failure", ex);
        }
        return false;

    }
}
