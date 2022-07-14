package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class HideCastButtonPatch {

    //Used by app.revanced.patches.youtube.layout.castbutton.patch.HideCastButonPatch
    public static int getCastButtonOverrideV2(int original) {
        return SettingsEnum.CAST_BUTTON_SHOWN.getBoolean() ? original : View.GONE;
    }
}
