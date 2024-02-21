package app.revanced.integrations.twitter.patches.links;

import android.content.Context;
import android.content.Intent;

public final class OpenLinksWithAppChooserPatch {
    public static void openWithChooser(final Context context, final Intent intent) {
        intent.setAction("android.intent.action.VIEW");

        context.startActivity(Intent.createChooser(intent, null));
    }
}
