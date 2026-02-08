package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class LoopVideoPatch {
    /**
     * Injection point
     */
    public static boolean shouldLoopVideo(Enum<?> status) {
        boolean shouldLoop = status != null && "ENDED".equals(status.name())
                && Settings.LOOP_VIDEO.get();

        // Instead of calling a method to loop the video, just seek to 00:00.
        if (shouldLoop) VideoInformation.seekTo(0);
        return shouldLoop;
    }
}
