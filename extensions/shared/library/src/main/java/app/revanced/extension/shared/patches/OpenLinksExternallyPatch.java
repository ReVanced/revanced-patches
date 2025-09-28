package app.revanced.extension.shared.patches;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import android.util.Log;

public class OpenLinksExternallyPatch {
    public static void openLink(final Context context, Intent intent) {
        try {
            Log.d("ReVanced", "Opening intent with chooser: " + intent);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception ex) {
            Logger.printException(() -> "OpenLinksExternally failure", ex);
        }
    }
    public static void openLink(final String url) {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        openLink(Utils.getContext(),intent);
    }
}
