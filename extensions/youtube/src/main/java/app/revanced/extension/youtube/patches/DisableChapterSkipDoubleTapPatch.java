package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class DisableChapterSkipDoubleTapPatch {

    /**
     * Injection point.
     *
     * @return If "should skip to chapter start" flag is set.
     */
    public static boolean disableDoubleTapChapters(boolean original) {
        return original && !Settings.DISABLE_CHAPTER_SKIP_DOUBLE_TAP.get();
    }
}