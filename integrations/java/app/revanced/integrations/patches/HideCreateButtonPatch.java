package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class HideCreateButtonPatch {

    //Used by app.revanced.patches.youtube.layout.createbutton.patch.CreateButtonRemoverPatch
    public static void hideCreateButton(View view) {
        boolean enabled = SettingsEnum.CREATE_BUTTON_ENABLED.getBoolean();
        String message =  "Create button: " + (enabled ? "shown" : "hidden");
        LogHelper.debug(HideCreateButtonPatch.class, message);
        view.setVisibility(enabled ? View.VISIBLE : View.GONE);
    }
}
