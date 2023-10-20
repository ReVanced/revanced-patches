package app.revanced.integrations.patches;

import app.revanced.integrations.shared.PlayerType;

public class MinimizedPlaybackPatch {

    /**
     * Injection point.
     */
    public static boolean videoSupportsMinimizedPlayback() {
        return !PlayerType.getCurrent().isNoneHiddenOrSlidingMinimized();
    }

    /**
     * Injection point.
     */
    public static boolean overrideMinimizedPlaybackAvailable() {
        // This could be done entirely in the patch,
        // but having a unique method to search for makes manually inspecting the patched apk much easier.
        return true;
    }

}
