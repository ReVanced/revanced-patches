package app.revanced.extension.music.patches;

import static app.revanced.extension.shared.Utils.hideViewBy0dpUnderCondition;

import android.view.View;
import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideCategoryBarPatch {

    /**
     * Injection point
     */
    public static void hideCategoryBar(View view) {
        hideViewBy0dpUnderCondition(Settings.HIDE_CATEGORY_BAR, view);
    }
}
