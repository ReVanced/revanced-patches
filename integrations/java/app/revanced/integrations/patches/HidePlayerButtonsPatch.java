package app.revanced.integrations.patches;


import app.revanced.integrations.settings.SettingsEnum;

public final class HidePlayerButtonsPatch {

    public static boolean hideButtons() {
        return SettingsEnum.HIDE_PLAYER_BUTTONS.getBoolean();
    }
}
