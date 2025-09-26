package app.revanced.extension.instagram.misc.links;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

public final class OpenLinksWithExternalBrowser {
    public static boolean openExternalBrowser(String url) {
        try {
            // The "url" parameter to this function will be of the form.
            // https://l.instagram.com/?u=<actual url>&e=<tracking id>
            String actualUrl = Uri.parse(url).getQueryParameter("u");
            if (actualUrl != null) {
                Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(actualUrl));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Utils.getContext().startActivity(intent);
                return true;
            }

        } catch (Exception ex) {
            Logger.printException(() -> "Instagram openWithBrowser failure", ex);
        }
        return false;

    }
}
