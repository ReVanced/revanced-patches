package app.revanced.integrations.tiktok.speed;

import app.revanced.integrations.tiktok.settings.Settings;

public class PlaybackSpeedPatch {
    public static void rememberPlaybackSpeed(float newSpeed) {
        Settings.REMEMBERED_SPEED.save(newSpeed);
    }

    public static float getPlaybackSpeed() {
        return Settings.REMEMBERED_SPEED.get();
    }
}
