package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

// Ideally this should be refactored into PlayerControlBottomButton.
public abstract class PlayerControlTopButton {
    static final int fadeInDuration  = 200; // Fade-in duration (ms)
    static final int fadeOutDuration = 600; // Fade-out duration (ms)

    private static final Animation fadeIn;
    private static final Animation fadeOut;

    private final WeakReference<ImageView> buttonReference;
    private boolean isShowing;

    static {
        fadeIn = Utils.getResourceAnimation("fade_in");
        fadeIn.setDuration(fadeInDuration);

        fadeOut = Utils.getResourceAnimation("fade_out");
        fadeOut.setDuration(fadeOutDuration);
    }

    public PlayerControlTopButton(ImageView imageView, View.OnClickListener onClickListener) {
        imageView.setVisibility(View.GONE);
        imageView.setOnClickListener(onClickListener);

        buttonReference = new WeakReference<>(imageView);
    }

    protected void setVisibilityImmediate(boolean visible) {
        private_setVisibility(visible, false);
    }

    protected void setVisibility(boolean visible, boolean animated) {
        // Ignore this call, otherwise with full screen thumbnails the buttons are visible while seeking.
        if (visible && !animated) return;

        private_setVisibility(visible, animated);
    }

    private void private_setVisibility(boolean visible, boolean animated) {
        try {
            if (isShowing == visible) return;
            isShowing = visible;

            ImageView iView = buttonReference.get();
            if (iView == null) return;

            if (visible) {
                iView.clearAnimation();
                if (!shouldBeShown()) {
                    return;
                }
                if (animated) {
                    iView.startAnimation(fadeIn);
                }
                iView.setVisibility(View.VISIBLE);
                return;
            }

            if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(fadeOut);
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "changeVisibility failure", ex);
        }
    }

    protected abstract boolean shouldBeShown();

    public void hide() {
        if (!isShowing) {
            return;
        }

        Utils.verifyOnMainThread();
        View v = buttonReference.get();
        if (v == null) {
            return;
        }
        v.setVisibility(View.GONE);
        isShowing = false;
    }
}
