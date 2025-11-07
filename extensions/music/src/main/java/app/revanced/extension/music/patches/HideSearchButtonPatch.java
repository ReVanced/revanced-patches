package app.revanced.extension.music.patches;

import static app.revanced.extension.shared.Utils.hideViewBy0dpUnderCondition;

import android.view.View;

import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideSearchButtonPatch {

    /**
     * Injection point
     */
    public static void hideSearchButton(View view) {
        hideViewBy0dpUnderCondition(Settings.HIDE_SEARCH_BUTTON, view);
    }
}
