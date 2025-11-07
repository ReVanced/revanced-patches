package app.revanced.extension.music.patches;

import static app.revanced.extension.shared.Utils.hideViewBy0dpUnderCondition;

import android.view.View;
import android.view.ViewGroup;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideNotificationButtonPatch {

    /**
     * Injection point
     */
    public static void hideNotificationButton(View view) {
        if (view.getParent() instanceof ViewGroup viewGroup) {
            hideViewBy0dpUnderCondition(Settings.HIDE_NOTIFICATION_BUTTON, viewGroup);
        }
    }
}
