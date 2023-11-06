package app.revanced.integrations.patches;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import app.revanced.integrations.settings.SettingsEnum;

/** @noinspection unused*/
public final class DisableSuggestedVideoEndScreenPatch {
    @SuppressLint("StaticFieldLeak")
    private static View lastView;

    public static void closeEndScreen(final ImageView imageView) {
        if (!SettingsEnum.DISABLE_SUGGESTED_VIDEO_END_SCREEN.getBoolean()) return;

        // Get a parent view which can be listened to for layout changes.
        final var parent = imageView.getParent().getParent();

        // Prevent adding the listener multiple times.
        if (lastView == parent) return;

        lastView = (ViewGroup)parent;
        lastView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop,
                                       int oldRight, int oldBottom) {
                // Disable sound effects to prevent the click sound.
                imageView.setSoundEffectsEnabled(false);
                imageView.performClick();

                // Remove the listener to prevent it from being called multiple times.
                lastView.removeOnLayoutChangeListener(this);
            }
        });
    }
}
