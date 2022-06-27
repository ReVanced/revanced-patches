package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class MiniplayerOverridePatch {

    //ToDo: Write Patch for it.
    //See https://drive.google.com/file/d/1G7jn2EdWgNls0Htgs-wPPjjObZL1emzK/view?usp=sharing
    //And https://drive.google.com/file/d/1-QlgSiKzqQ5lHXQnvRUpijk0GH9T1Sn7/view?usp=sharing
    // for where it needs to be used.
    public static boolean getTabletMiniplayerOverride() {
        return SettingsEnum.USE_TABLET_MINIPLAYER_BOOLEAN.getBoolean();
    }
}
