package app.revanced.integrations.patches;

import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;

public final class FixPlaybackPatch {
    private static Thread currentThread = null;
    private static String videoId;

    public static void newVideoLoaded(final String videoId) {
        if (!SettingsEnum.FIX_PLAYBACK.getBoolean()) return;

        if (videoId.equals(FixPlaybackPatch.videoId)) return;
        else FixPlaybackPatch.videoId = videoId;

        if (currentThread != null) {
            currentThread.interrupt();
        }

        currentThread = new Thread(() -> {
            try {
                while (true) {
                    var currentVideoTime = VideoInformation.getVideoTime();

                    if (currentVideoTime > -1) {
                        VideoInformation.seekTo(Integer.MAX_VALUE);
                        VideoInformation.seekTo(currentVideoTime);
                        return;
                    }

                    Thread.sleep(10);
                }
            } catch (InterruptedException e) {
                LogHelper.printDebug(() -> "Thread was interrupted");
            }
        });

        currentThread.start();
    }
}
