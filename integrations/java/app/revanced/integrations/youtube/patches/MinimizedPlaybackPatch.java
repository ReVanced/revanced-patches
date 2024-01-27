package app.revanced.integrations.youtube.patches;

import app.revanced.integrations.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class MinimizedPlaybackPatch {

    /**
     * Injection point.
     */
    public static boolean playbackIsNotShort() {
        // Steps to verify most edge cases:
        // 1. Open a regular video
        // 2. Minimize app (PIP should appear)
        // 3. Reopen app
        // 4. Open a Short (without closing the regular video)
        //    (try opening both Shorts in the video player suggestions AND Shorts from the home feed)
        // 5. Minimize the app (PIP should not appear)
        // 6. Reopen app
        // 7. Close the Short
        // 8. Resume playing the regular video
        // 9. Minimize the app (PIP should appear)

        if (!VideoInformation.lastVideoIdIsShort()) {
            return true; // Definitely is not a Short.
        }

        // Might be a Short, or might be a prior regular video on screen again after a Short was closed.
        // This incorrectly prevents PIP if player is in WATCH_WHILE_MINIMIZED after closing a Short,
        // But there's no way around this unless an additional hook is added to definitively detect
        // the Shorts player is on screen. This use case is unusual anyways so it's not a huge concern.
        return !PlayerType.getCurrent().isNoneHiddenOrMinimized();
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
