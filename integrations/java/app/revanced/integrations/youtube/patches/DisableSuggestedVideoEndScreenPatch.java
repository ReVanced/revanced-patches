package app.revanced.integrations.youtube.patches;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import app.revanced.integrations.youtube.settings.Settings;

/** @noinspection unused*/
public final class DisableSuggestedVideoEndScreenPatch {
    @SuppressLint("StaticFieldLeak")
    private static View lastView;

    public static void closeEndScreen(final ImageView imageView) {
        if (!Settings.DISABLE_SUGGESTED_VIDEO_END_SCREEN.get()) return;

        // Get a parent view which can be listened to for layout changes.
        final var parent = imageView.getParent().getParent();

        // Prevent adding the listener multiple times.
        if (lastView == parent) return;

        lastView = (ViewGroup)parent;
        lastView.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            // Disable sound effects to prevent the click sound.
            imageView.setSoundEffectsEnabled(false);
            imageView.performClick();
        });
    }
}
