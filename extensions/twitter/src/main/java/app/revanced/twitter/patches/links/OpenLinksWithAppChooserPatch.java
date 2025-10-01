package app.revanced.twitter.patches.links;

import android.content.Context;
import android.content.Intent;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
@Deprecated(forRemoval = true)
public final class OpenLinksWithAppChooserPatch {

    /**
     * Injection point.
     */
    public static void openWithChooser(final Context context, final Intent intent) {
        Logger.printInfo(() -> "Opening intent with chooser: " + intent);

        intent.setAction("android.intent.action.VIEW");

        context.startActivity(Intent.createChooser(intent, null));
    }
}
