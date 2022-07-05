package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.adremover.AdRemoverAPI;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class HideCreateButtonPatch {

    //Used by app.revanced.patches.youtube.layout.createbutton.patch.CreateButtonRemoverPatch
    public static void hideCreateButton(View view) {
        String message = SettingsEnum.CREATE_BUTTON_SHOWN_BOOLEAN.getBoolean() ? "Create button: Shown" : "Create button: Hidden";
        LogHelper.debug(HideCreateButtonPatch.class, message);
        if (SettingsEnum.CREATE_BUTTON_SHOWN_BOOLEAN.getBoolean()) {
            view.setVisibility(View.VISIBLE);
        } else {
            view.setVisibility(View.GONE);
        }
    }
}
