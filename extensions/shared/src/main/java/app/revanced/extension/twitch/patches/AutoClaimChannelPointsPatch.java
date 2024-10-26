package app.revanced.extension.twitch.patches;

import app.revanced.extension.twitch.settings.Settings;

@SuppressWarnings("unused")
public class AutoClaimChannelPointsPatch {
    public static boolean shouldAutoClaim() {
        return Settings.AUTO_CLAIM_CHANNEL_POINTS.get();
    }
}
