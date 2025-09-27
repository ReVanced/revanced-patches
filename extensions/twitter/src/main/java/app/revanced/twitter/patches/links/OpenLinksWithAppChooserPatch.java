package app.revanced.twitter.patches.links;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import app.revanced.extension.shared.patches.OpenLinksExternally;

public final class OpenLinksWithAppChooserPatch {
    public static void openWithChooser(final Context context, final Intent intent) {
        intent.setAction("android.intent.action.VIEW");
        OpenLinksExternally.openLink(context,intent);
    }
}
