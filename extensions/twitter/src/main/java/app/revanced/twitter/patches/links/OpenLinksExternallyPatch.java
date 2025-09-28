package app.revanced.twitter.patches.links;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class OpenLinksExternallyPatch {
    public static void openWithChooser(final Context context, final Intent intent) {
        intent.setAction("android.intent.action.VIEW");
        app.revanced.extension.shared.patches.OpenLinksExternallyPatch.openLink(context,intent);
    }
}
