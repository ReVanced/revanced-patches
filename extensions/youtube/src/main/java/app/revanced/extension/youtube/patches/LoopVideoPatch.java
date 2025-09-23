package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class LoopVideoPatch {
    /**
     * Injection point
     */
    public static boolean shouldLoopVideo() {
        return Settings.LOOP_VIDEO.get();
    }
}
