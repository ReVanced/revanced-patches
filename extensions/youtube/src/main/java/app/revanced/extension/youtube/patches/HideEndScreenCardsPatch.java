package app.revanced.extension.youtube.patches;

import android.view.View;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class HideEndScreenCardsPatch {

    /**
     * Injection point.
     */
    public static void hideEndScreenCardView(View view) {
        Utils.hideViewUnderCondition(Settings.HIDE_ENDSCREEN_CARDS, view);
    }

    /**
     * Injection point.
     */
    public static boolean hideEndScreenCards() {
        return Settings.HIDE_ENDSCREEN_CARDS.get();
    }
}