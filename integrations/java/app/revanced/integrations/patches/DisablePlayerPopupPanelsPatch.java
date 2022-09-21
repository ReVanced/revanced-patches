package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;

public class DisablePlayerPopupPanelsPatch {
    //Used by app.revanced.patches.youtube.layout.playerpopuppanels.patch.PlayerPopupPanelsPatch
    public static boolean disablePlayerPopupPanels() {
        return SettingsEnum.PLAYER_POPUP_PANELS.getBoolean();
    }
}
