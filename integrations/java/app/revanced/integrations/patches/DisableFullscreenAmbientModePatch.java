package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

/** @noinspection unused*/
public final class DisableFullscreenAmbientModePatch {
    public static boolean enableFullScreenAmbientMode() {
        return !SettingsEnum.DISABLE_FULLSCREEN_AMBIENT_MODE.getBoolean();
    }
}
