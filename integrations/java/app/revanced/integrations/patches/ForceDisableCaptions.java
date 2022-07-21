package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class ForceDisableCaptions {

    //ToDo: Write Patch for it
    public static boolean captionsEnabled() {
        return SettingsEnum.CAPTIONS_ENABLED.getBoolean();
    }

}
