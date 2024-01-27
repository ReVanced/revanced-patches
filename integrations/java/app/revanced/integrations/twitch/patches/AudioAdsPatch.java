package app.revanced.integrations.twitch.patches;

import app.revanced.integrations.twitch.settings.Settings;

@SuppressWarnings("unused")
public class AudioAdsPatch {
    public static boolean shouldBlockAudioAds() {
        return Settings.BLOCK_AUDIO_ADS.get();
    }
}
