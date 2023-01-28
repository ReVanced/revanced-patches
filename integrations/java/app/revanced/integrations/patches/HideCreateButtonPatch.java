package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public class HideCreateButtonPatch {

    //Used by app.revanced.patches.youtube.layout.createbutton.patch.CreateButtonRemoverPatch
    public static void hideCreateButton(View view) {
        boolean hidden = SettingsEnum.HIDE_CREATE_BUTTON.getBoolean();
        LogHelper.printDebug(() -> "Create button: " + (hidden ? "hidden" : "shown"));
        view.setVisibility(hidden ? View.GONE : View.VISIBLE);
    }
}
