package app.revanced.integrations.youtube.patches;

import android.content.Intent;

import app.revanced.integrations.youtube.settings.Settings;
import app.revanced.integrations.shared.Logger;

@SuppressWarnings("unused")
public final class ChangeStartPagePatch {
    public static void changeIntent(Intent intent) {
        final var startPage = Settings.START_PAGE.get();
        if (startPage.isEmpty()) return;

        Logger.printDebug(() -> "Changing start page to " + startPage);
        intent.setAction("com.google.android.youtube.action." + startPage);
    }
}
