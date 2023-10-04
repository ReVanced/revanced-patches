package app.revanced.integrations.patches.spoof;

import app.revanced.integrations.settings.SettingsEnum;

public class SpoofAppVersionPatch {

    public static String getYouTubeVersionOverride(String version) {
        if (SettingsEnum.SPOOF_APP_VERSION.getBoolean()) {
            return SettingsEnum.SPOOF_APP_VERSION_TARGET.getString();
        }
        return version;
    }
}
