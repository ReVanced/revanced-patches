package app.revanced.integrations.youtube.patches;

import android.view.View;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class FullscreenPanelsRemoverPatch {
    public static int getFullscreenPanelsVisibility() {
        return Settings.HIDE_FULLSCREEN_PANELS.get() ? View.GONE : View.VISIBLE;
    }
}
