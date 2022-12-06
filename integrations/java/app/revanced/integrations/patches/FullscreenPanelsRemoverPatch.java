package app.revanced.integrations.patches;

import android.view.View;

import app.revanced.integrations.settings.SettingsEnum;

public class FullscreenPanelsRemoverPatch {

    public static int getFullscreenPanelsVisibility() {
        return SettingsEnum.HIDE_FULLSCREEN_PANELS.getBoolean() ? View.GONE : View.VISIBLE;
    }

}
