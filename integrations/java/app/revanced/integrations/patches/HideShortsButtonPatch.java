package app.revanced.integrations.patches;


import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class HideShortsButtonPatch {

    //Todo: Switch BooleanPreferences to Settings class
    //Used by app.revanced.patches.youtube.layout.shorts.button.patch.ShortsButtonRemoverPatch
    public static void hideShortsButton(View view) {
        if (lastPivotTab != null && lastPivotTab.name() == "TAB_SHORTS") {
            String message = SettingsEnum.SHORTS_BUTTON_SHOWN_BOOLEAN.getBoolean() ? "Shorts button: shown" : "Shorts button: hidden";
            LogHelper.debug(HideShortsButtonPatch.class, message);
            if (!SettingsEnum.SHORTS_BUTTON_SHOWN_BOOLEAN.getBoolean()) {
                view.setVisibility(View.GONE);
            }
        }
    }

    //Needed for the ShortsButtonRemoverPatch
    public static Enum lastPivotTab;
}
