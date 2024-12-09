package app.revanced.extension.tiktok.speed;

import app.revanced.extension.tiktok.settings.Settings;

public class PlaybackSpeedPatch {
    public static void rememberPlaybackSpeed(float newSpeed) {
        Settings.REMEMBERED_SPEED.save(newSpeed);
    }

    public static float getPlaybackSpeed() {
        return Settings.REMEMBERED_SPEED.get();
    }
}
