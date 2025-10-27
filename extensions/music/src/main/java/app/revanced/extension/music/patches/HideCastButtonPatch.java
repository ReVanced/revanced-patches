package app.revanced.extension.music.patches;

import static app.revanced.extension.shared.Utils.hideViewBy0dpUnderCondition;

import android.view.View;
import app.revanced.extension.music.settings.Settings;

@SuppressWarnings("unused")
public class HideCastButtonPatch {

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
}
