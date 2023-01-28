package app.revanced.integrations.patches;


import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class HideShortsButtonPatch {

    // Used by app.revanced.patches.youtube.layout.shorts.button.patch.ShortsButtonRemoverPatch
    public static void hideShortsButton(View view) {
        if (lastPivotTab != null && lastPivotTab.name() == "TAB_SHORTS") {
            boolean hide = SettingsEnum.HIDE_SHORTS_BUTTON.getBoolean();
            LogHelper.printDebug(() -> hide ? "Shorts button: hidden" : "Shorts button: shown");
            if (hide) {
                view.setVisibility(View.GONE);
            }
        }
    }

    //Needed for the ShortsButtonRemoverPatch
    public static Enum lastPivotTab;
}
