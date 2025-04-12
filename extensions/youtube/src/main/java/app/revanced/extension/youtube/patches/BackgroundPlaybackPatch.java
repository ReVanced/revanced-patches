package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.PlayerType;
import app.revanced.extension.youtube.shared.ShortsPlayerState;

@SuppressWarnings("unused")
public class BackgroundPlaybackPatch {

    /**
     * Injection point.
     */
    public static boolean isBackgroundPlaybackAllowed(boolean original) {
        if (original) return true;

        // Steps to verify most edge cases (with Shorts background playback set to off):
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
        if (ShortsPlayerState.isOpen()) {
            return false;
        }

        // Check if the video player is opened and it's not playing in the feed.
        PlayerType current = PlayerType.getCurrent();
        return !current.isNoneOrHidden() && current != PlayerType.INLINE_MINIMAL;
    }

    /**
     * Injection point.
     */
    public static boolean isBackgroundShortsPlaybackAllowed(boolean original) {
        return !Settings.DISABLE_SHORTS_BACKGROUND_PLAYBACK.get();
    }
}
