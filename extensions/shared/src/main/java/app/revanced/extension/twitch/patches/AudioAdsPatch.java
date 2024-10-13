package app.revanced.extension.twitch.patches;

import app.revanced.extension.twitch.settings.Settings;

@SuppressWarnings("unused")
public class AudioAdsPatch {
    public static boolean shouldBlockAudioAds() {
        return Settings.BLOCK_AUDIO_ADS.get();
    }
}
