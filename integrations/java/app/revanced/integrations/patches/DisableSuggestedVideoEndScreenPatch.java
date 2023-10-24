package app.revanced.integrations.patches;

import android.widget.ImageView;
import app.revanced.integrations.settings.SettingsEnum;

/** @noinspection unused*/
public final class DisableSuggestedVideoEndScreenPatch {
    public static void closeEndScreen(ImageView imageView) {
        if (!SettingsEnum.DISABLE_SUGGESTED_VIDEO_END_SCREEN.getBoolean()) return;

        imageView.performClick();
    }
}
