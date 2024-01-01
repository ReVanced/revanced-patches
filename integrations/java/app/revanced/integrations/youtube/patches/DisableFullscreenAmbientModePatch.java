package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

/** @noinspection unused*/
public final class DisableFullscreenAmbientModePatch {
    public static boolean enableFullScreenAmbientMode() {
        return !Settings.DISABLE_FULLSCREEN_AMBIENT_MODE.get();
    }
}
