package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public class DisablePlayerPopupPanelsPatch {
    //Used by app.revanced.patches.youtube.layout.playerpopuppanels.patch.PlayerPopupPanelsPatch
    public static boolean disablePlayerPopupPanels() {
        return Settings.PLAYER_POPUP_PANELS.get();
    }
}
