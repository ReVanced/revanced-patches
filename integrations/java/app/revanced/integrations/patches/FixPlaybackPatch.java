package app.revanced.integrations.patches;

import java.util.Timer;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public final class FixPlaybackPatch {
    private static Thread currentThread = null;
    public static void newVideoLoaded(final String _videoId) {
        if (!SettingsEnum.FIX_PLAYBACK.getBoolean()) return;

        if (currentThread != null) {
            currentThread.interrupt();
        }

        currentThread = new Thread(() -> {
            while (true) {
                var currentVideoLength = PlayerControllerPatch.getCurrentVideoLength();
                if (currentVideoLength > 1) {
                    PlayerControllerPatch.seekTo(currentVideoLength);
                    PlayerControllerPatch.seekTo(1);
                    return;
                }

                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    LogHelper.debug(FixPlaybackPatch.class, "Thread was interrupted");
                }
            }
        });

        currentThread.start();
    }
}
