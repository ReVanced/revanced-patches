package app.revanced.integrations.youtube.patches;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.youtube.settings.Settings;

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
