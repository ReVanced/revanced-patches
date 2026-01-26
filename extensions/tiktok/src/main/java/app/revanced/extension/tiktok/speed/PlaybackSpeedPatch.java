package app.revanced.extension.tiktok.speed;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.tiktok.settings.Settings;

public class PlaybackSpeedPatch {
    private static volatile float lastLoggedSpeed = Float.NaN;

    public static void rememberPlaybackSpeed(float newSpeed) {
        if (BaseSettings.DEBUG.get()) {
            float oldSpeed = Settings.REMEMBERED_SPEED.get();
            Logger.printInfo(() -> "[ReVanced PlaybackSpeed] remember speed " + oldSpeed + " -> " + newSpeed);
        }
        Settings.REMEMBERED_SPEED.save(newSpeed);
    }

    public static float getPlaybackSpeed() {
        float speed = Settings.REMEMBERED_SPEED.get();
        if (BaseSettings.DEBUG.get() && Float.compare(lastLoggedSpeed, speed) != 0) {
            lastLoggedSpeed = speed;
            Logger.printInfo(() -> "[ReVanced PlaybackSpeed] get speed=" + speed);
        }
        return speed;
    }
}
