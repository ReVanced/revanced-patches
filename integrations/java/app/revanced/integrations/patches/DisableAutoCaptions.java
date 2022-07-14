package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class DisableAutoCaptions {

    //ToDo: Write Patch for it
    public static boolean autoCaptionsEnabled() {
        return SettingsEnum.AUTO_CAPTIONS_ENABLED.getBoolean();
    }

}
