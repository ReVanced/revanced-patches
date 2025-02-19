package app.revanced.extension.youtube.videoplayer;

import android.transition.Fade;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

// Ideally this should be refactored into PlayerControlBottomButton,
// but the show/hide logic is not the same so keeping this as two classes might be simpler.
public abstract class PlayerControlTopButton {
    static final int fadeInDuration;
    static final int fadeOutDuration;

    private static final Animation fadeInAnimation;
    private static final Animation fadeOutAnimation;

    static final Fade fadeInTransition;
    static final Fade fadeOutTransition;

    private final WeakReference<ImageView> buttonReference;
    private boolean isShowing;

    static {
        fadeInDuration = Utils.getResourceInteger("fade_duration_fast");
        fadeOutDuration = Utils.getResourceInteger("fade_duration_scheduled");

        fadeInAnimation = Utils.getResourceAnimation("fade_in");
        fadeInAnimation.setDuration(fadeInDuration);

        fadeOutAnimation = Utils.getResourceAnimation("fade_out");
        fadeOutAnimation.setDuration(fadeOutDuration);

        fadeInTransition = new Fade();
        fadeInTransition.setDuration(fadeInDuration);

        fadeOutTransition = new Fade();
        fadeOutTransition.setDuration(fadeOutDuration);
    }

    protected PlayerControlTopButton(ImageView imageView, View.OnClickListener onClickListener) {
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
                    iView.startAnimation(fadeInAnimation);
                }
                iView.setVisibility(View.VISIBLE);
                return;
            }

            if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(fadeOutAnimation);
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "private_setVisibility failure", ex);
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
