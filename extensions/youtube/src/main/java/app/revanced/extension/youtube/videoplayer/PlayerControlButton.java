package app.revanced.extension.youtube.videoplayer;

import android.transition.Fade;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

public abstract class PlayerControlButton {
    public interface PlayerControlButtonVisibility {
        boolean shouldBeShown();
    }

    private static final int fadeInDuration;
    private static final int fadeOutDuration;

    private static final Animation fadeInAnimation;
    private static final Animation fadeOutAnimation;
    private static final Animation fadeOutImmediate;

    private static final Fade fadeInTransition;
    private static final Fade fadeOutTransition;

    static {
        fadeInDuration = Utils.getResourceInteger("fade_duration_fast");
        fadeOutDuration = Utils.getResourceInteger("fade_duration_scheduled");

        fadeInAnimation = Utils.getResourceAnimation("fade_in");
        fadeInAnimation.setDuration(fadeInDuration);

        fadeOutAnimation = Utils.getResourceAnimation("fade_out");
        fadeOutAnimation.setDuration(fadeOutDuration);

        // Animation for the fast fade out after tappoing the overlay.
        // Currently not used but should be.
        fadeOutImmediate = Utils.getResourceAnimation("abc_fade_out");
        fadeOutImmediate.setDuration(Utils.getResourceInteger("fade_duration_fast"));

        fadeInTransition = new Fade();
        fadeInTransition.setDuration(fadeInDuration);

        fadeOutTransition = new Fade();
        fadeOutTransition.setDuration(fadeOutDuration);
    }

    private final WeakReference<ImageView> buttonRef;
    private final PlayerControlButtonVisibility visibilityCheck;
    private boolean isVisible;

    protected PlayerControlButton(View controlsViewGroup,
                                  String imageViewButtonId,
                                  PlayerControlButtonVisibility buttonVisibility,
                                  View.OnClickListener onClickListener,
                                  @Nullable View.OnLongClickListener longClickListener) {
        Logger.printDebug(() -> "Initializing button: " + imageViewButtonId);

        ImageView imageView = Objects.requireNonNull(controlsViewGroup.findViewById(
                Utils.getResourceIdentifier(imageViewButtonId, "id")
        ));
        imageView.setVisibility(View.GONE);

        imageView.setOnClickListener(onClickListener);
        if (longClickListener != null) {
            imageView.setOnLongClickListener(longClickListener);
        }

        visibilityCheck = buttonVisibility;
        buttonRef = new WeakReference<>(imageView);
    }

    protected void setVisibilityImmediate(boolean visible) {
        if (visible) {
            // Fix button flickering, by pushing this call to the back of
            // the main thread and letting other layout code run first.
            Utils.runOnMainThread(() -> private_setVisibility(true, false));
        } else {
            private_setVisibility(false, false);
        }
    }

    protected void setVisibility(boolean visible, boolean animated) {
        // Ignore this call, otherwise with full screen thumbnails the buttons are visible while seeking.
        if (visible && !animated) return;

        private_setVisibility(visible, animated);
    }

    private void private_setVisibility(boolean visible, boolean animated) {
        try {
            // If the visibility state hasn't changed, return early.
            if (isVisible == visible) return;
            isVisible = visible;

            ImageView iView = buttonRef.get();
            if (iView == null) {
                return;
            }

            if (visible && visibilityCheck.shouldBeShown()) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(PlayerControlButton.fadeInAnimation);
                }
                iView.setVisibility(View.VISIBLE);
            } else if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(PlayerControlButton.fadeOutAnimation);
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "private_setVisibility failure", ex);
        }
    }

    public void hide() {
        if (!isVisible) {
            return;
        }

        Utils.verifyOnMainThread();
        View v = buttonRef.get();
        if (v == null) {
            return;
        }
        v.setVisibility(View.GONE);
        isVisible = false;
    }
}
