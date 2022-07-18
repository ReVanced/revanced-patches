package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class FullscreenPanelsRemoverPatch {

    public int getFullsceenPanelsVisibility() {
        return SettingsEnum.FULLSCREEN_PANELS_SHOWN.getBoolean() ? View.VISIBLE : View.GONE;
    }

}
