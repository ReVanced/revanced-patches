package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.Settings;

@SuppressWarnings("unused")
public class AutoClaimChannelPointsPatch {
    public static boolean shouldAutoClaim() {
        return Settings.AUTO_CLAIM_CHANNEL_POINTS.get();
    }
}
