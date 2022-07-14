package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;

public class HideReelsPatch {

    /**
     * Used by app.revanced.patches.youtube.layout.reels.patch.HideReelsPatch
     *
     * @param view
     */
    public static void HideReel(View view) {
        if (!SettingsEnum.REEL_BUTTON_SHOWN.getBoolean()) {
            AdRemoverAPI.HideViewWithLayout1dp(view);
        }
    }
}
