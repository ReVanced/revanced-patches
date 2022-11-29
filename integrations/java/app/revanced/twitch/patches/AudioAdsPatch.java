package app.revanced.twitch.patches;

import app.revanced.twitch.settings.SettingsEnum;

public class AudioAdsPatch {
    public static boolean shouldBlockAudioAds() {
        return SettingsEnum.BLOCK_AUDIO_ADS.getBoolean();
    }
}
