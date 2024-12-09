package app.revanced.twitter.patches.links;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

public final class OpenLinksWithAppChooserPatch {
    public static void openWithChooser(final Context context, final Intent intent) {
        Log.d("ReVanced", "Opening intent with chooser: " + intent);

        intent.setAction("android.intent.action.VIEW");

        context.startActivity(Intent.createChooser(intent, null));
    }
}
