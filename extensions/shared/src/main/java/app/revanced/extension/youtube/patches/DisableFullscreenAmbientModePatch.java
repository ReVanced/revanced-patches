package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

/** @noinspection unused*/
public final class DisableFullscreenAmbientModePatch {
    public static boolean enableFullScreenAmbientMode() {
        return !Settings.DISABLE_FULLSCREEN_AMBIENT_MODE.get();
    }
}
