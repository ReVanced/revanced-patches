package app.revanced.extension.youtube.patches;

import androidx.annotation.Nullable;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public class OpenVideosFullscreenHookPatch {

    @Nullable
    private static volatile Boolean openNextVideoFullscreen;

    public static void setOpenNextVideoFullscreen(@Nullable Boolean forceFullScreen) {
        openNextVideoFullscreen = forceFullScreen;
    }

    /**
     * Changed during patching since this class is also
     * used by {@link OpenVideosFullscreenHookPatch}.
     */
    private static boolean isFullScreenPatchIncluded() {
        return false;
    }

    /**
     * Injection point.
     *
     * Returns negated value.
     */
    public static boolean doNotOpenVideoFullscreenPortrait(boolean original) {
        Boolean openFullscreen = openNextVideoFullscreen;
        if (openFullscreen != null) {
            openNextVideoFullscreen = null;
            return !openFullscreen;
        }

        if (!isFullScreenPatchIncluded()) {
            return original;
        }

        return !Settings.OPEN_VIDEOS_FULLSCREEN_PORTRAIT.get();
    }
}
