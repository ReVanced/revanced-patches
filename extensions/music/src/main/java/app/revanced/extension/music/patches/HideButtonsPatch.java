package app.revanced.extension.music.patches;

import static app.revanced.extension.shared.Utils.hideViewBy0dpUnderCondition;

import android.view.View;
import android.view.ViewGroup;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideButtonsPatch {

    /**
     * Injection point
     */
    public static int hideCastButton(int original) {
        return Settings.HIDE_CAST_BUTTON.get() ? View.GONE : original;
    }

    /**
     * Injection point
     */
    public static void hideCastButton(View view) {
        hideViewBy0dpUnderCondition(Settings.HIDE_CAST_BUTTON, view);
    }

    /**
     * Injection point
     */
    public static boolean hideHistoryButton(boolean original) {
        return original && !Settings.HIDE_HISTORY_BUTTON.get();
    }

    /**
     * Injection point
     */
    public static void hideNotificationButton(View view) {
        if (view.getParent() instanceof ViewGroup viewGroup) {
            hideViewBy0dpUnderCondition(Settings.HIDE_NOTIFICATION_BUTTON, viewGroup);
        }
    }

    /**
     * Injection point
     */
    public static void hideSearchButton(View view) {
        hideViewBy0dpUnderCondition(Settings.HIDE_SEARCH_BUTTON, view);
    }
}
