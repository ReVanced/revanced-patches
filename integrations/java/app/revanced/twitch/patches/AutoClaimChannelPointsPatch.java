package app.revanced.twitch.patches;

import app.revanced.twitch.settings.SettingsEnum;

public class AutoClaimChannelPointsPatch {
    public static boolean shouldAutoClaim() {
        return SettingsEnum.AUTO_CLAIM_CHANNEL_POINTS.getBoolean();
    }
}
