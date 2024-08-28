package app.revanced.extension.youtube.patches;

import android.annotation.SuppressLint;
import android.widget.ImageView;

import app.revanced.extension.youtube.settings.Settings;

/** @noinspection unused*/
public final class DisableSuggestedVideoEndScreenPatch {
    @SuppressLint("StaticFieldLeak")
    private static ImageView lastView;

    public static void closeEndScreen(final ImageView imageView) {
        if (!Settings.DISABLE_SUGGESTED_VIDEO_END_SCREEN.get()) return;

        // Prevent adding the listener multiple times.
        if (lastView == imageView) return;
        lastView = imageView;

        imageView.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            if (imageView.isShown()) imageView.callOnClick();
        });
    }
}
