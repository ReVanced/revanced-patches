package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;
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

    static {
        fadeInDuration = Utils.getResourceInteger("fade_duration_fast");
        fadeOutDuration = Utils.getResourceInteger("fade_duration_scheduled");

        fadeInAnimation = Utils.getResourceAnimation("fade_in");
        fadeInAnimation.setDuration(fadeInDuration);

        fadeOutAnimation = Utils.getResourceAnimation("fade_out");
        fadeOutAnimation.setDuration(fadeOutDuration);

        // Animation for the fast fade out after tapping the overlay.
        // Currently not used but should be.
        fadeOutImmediate = Utils.getResourceAnimation("abc_fade_out");
        fadeOutImmediate.setDuration(Utils.getResourceInteger("fade_duration_fast"));
    }

    private final WeakReference<ImageView> buttonRef;
    private final PlayerControlButtonVisibility visibilityCheck;
    private final WeakReference<ImageView> placeHolderRef;
    private boolean isVisible;

    protected PlayerControlButton(View controlsViewGroup,
                                  String imageViewButtonId,
                                  @Nullable String placeholderId,
                                  PlayerControlButtonVisibility buttonVisibility,
                                  View.OnClickListener onClickListener,
                                  @Nullable View.OnLongClickListener longClickListener) {
        Logger.printDebug(() -> "Initializing button: " + imageViewButtonId);

        ImageView imageView = Objects.requireNonNull(controlsViewGroup.findViewById(
                Utils.getResourceIdentifier(imageViewButtonId, "id")
        ));
        imageView.setVisibility(View.GONE);

        ImageView tempPlaceholder = null;
        if (placeholderId != null) {
            tempPlaceholder = controlsViewGroup.findViewById(
                    Utils.getResourceIdentifier(placeholderId, "id")
            );
            if (tempPlaceholder != null) {
                tempPlaceholder.setVisibility(View.GONE);
            }
        }
        placeHolderRef = new WeakReference<>(tempPlaceholder);

        imageView.setOnClickListener(onClickListener);
        if (longClickListener != null) {
            imageView.setOnLongClickListener(longClickListener);
        }

        visibilityCheck = buttonVisibility;
        buttonRef = new WeakReference<>(imageView);
        isVisible = false;
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
            if (isVisible == visible) return;
            isVisible = visible;

            ImageView iView = buttonRef.get();
            if (iView == null) return;

            ImageView placeholder = placeHolderRef.get();

            if (visible && visibilityCheck.shouldBeShown()) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(PlayerControlButton.fadeInAnimation);
                }
                iView.setVisibility(View.VISIBLE);

                if (placeholder != null) {
                    placeholder.setVisibility(View.GONE);
                }
            } else if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(PlayerControlButton.fadeOutAnimation);
                }
                iView.setVisibility(View.GONE);

                if (placeholder != null) {
                    placeholder.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "private_setVisibility failure", ex);
        }
    }

    public void hide() {
        if (!isVisible) return;

        Utils.verifyOnMainThread();
        View view = buttonRef.get();
        if (view == null) return;
        view.setVisibility(View.GONE);

        view = placeHolderRef.get();
        if (view != null) view.setVisibility(View.VISIBLE);
        isVisible = false;
    }
}