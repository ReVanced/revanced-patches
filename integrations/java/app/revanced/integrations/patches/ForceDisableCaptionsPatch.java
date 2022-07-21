package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class ForceDisableCaptionsPatch {
    
    public static boolean captionsEnabled() {
        return SettingsEnum.CAPTIONS_ENABLED.getBoolean();
    }

}
