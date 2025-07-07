package app.revanced.extension.youtube.patches;

import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class DisableChapterSkipDoubleTapPatch {
    // Returns the updated value for the "should skip to chapter start" flag
    public static boolean disableDoubleTapChapters(boolean original) {
        return original && !Settings.DISABLE_CHAPTER_SKIP_DOUBLE_TAP.get();
    }
}