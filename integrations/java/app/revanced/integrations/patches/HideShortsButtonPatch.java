package app.revanced.integrations.patches;


import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class HideShortsButtonPatch {

    //Used by app.revanced.patches.youtube.layout.shorts.button.patch.ShortsButtonRemoverPatch
    public static void hideShortsButton(View view) {
        if (lastPivotTab != null && lastPivotTab.name() == "TAB_SHORTS") {
            boolean show = SettingsEnum.SHORTS_BUTTON_SHOWN.getBoolean();
            String message = show ? "Shorts button: shown" : "Shorts button: hidden";
            LogHelper.debug(HideShortsButtonPatch.class, message);
            if (!show) {
                view.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        }
    }

    //Needed for the ShortsButtonRemoverPatch
    public static Enum lastPivotTab;
}
