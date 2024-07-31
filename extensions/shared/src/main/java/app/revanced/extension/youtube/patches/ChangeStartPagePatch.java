package app.revanced.extension.youtube.patches;

import android.content.Intent;
import android.net.Uri;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class ChangeStartPagePatch {
    public static void changeIntent(final Intent intent) {
        final var startPage = Settings.START_PAGE.get();
        if (startPage.isEmpty()) return;

        Logger.printDebug(() -> "Changing start page to " + startPage);

        if (startPage.startsWith("www"))
            intent.setData(Uri.parse(startPage));
        else
            intent.setAction("com.google.android.youtube.action." + startPage);
    }
}
