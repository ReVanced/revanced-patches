package app.revanced.integrations.patches;

import android.content.Intent;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

@SuppressWarnings("unused")
public final class ChangeStartPagePatch {
    public static void changeIntent(Intent intent) {
        final var startPage = SettingsEnum.START_PAGE.getString();
        if (startPage.isEmpty()) return;

        LogHelper.printDebug(() -> "Changing start page to " + startPage);
        intent.setAction("com.google.android.youtube.action." + startPage);
    }
}
