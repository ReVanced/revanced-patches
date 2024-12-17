package app.revanced.extension.youtube.patches;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class OpenVideosFullscreenHookPatch {

    @Nullable
    private static volatile Boolean openNextShortFullscreen;

    public static void setOpenNextShortFullscreen(@Nullable Boolean forceFullScreen) {
        openNextShortFullscreen = forceFullScreen;
    }

    /**
     * Changed by 'Open videos fullscreen' patch,
     * as this class is also used by 'Open Shorts in regular player' patch.
     */
    private static boolean isFullScreenPatchIncluded() {
        return false; // Modified by patches.
    }

    /**
     * Injection point.
     */
    public static boolean openVideoFullscreenPortrait(boolean original) {
        Boolean openFullscreen = openNextShortFullscreen;
        if (openFullscreen != null) {
            openNextShortFullscreen = null;
            return openFullscreen;
        }

        if (!isFullScreenPatchIncluded()) {
            return false;
        }

        return Settings.OPEN_VIDEOS_FULLSCREEN_PORTRAIT.get();
    }
}
