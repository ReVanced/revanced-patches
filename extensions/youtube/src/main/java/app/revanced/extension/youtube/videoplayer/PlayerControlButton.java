package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;

public class PlayerControlButton {
    public interface PlayerControlButtonVisibility {
        /**
         * @return If the button should be shown when the player overlay is visible.
         */
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

    private final WeakReference<View> buttonRef;
    /**
     * Empty view with the same layout size as the button. Used to fill empty space while the
     * fade out animation runs. Without this the chapter titles overlapping the button when fading out.
     */
    private final WeakReference<View> placeHolderRef;
    private final PlayerControlButtonVisibility visibilityCheck;
    private boolean isVisible;

    public PlayerControlButton(View controlsViewGroup,
                               String imageViewButtonId,
                               @Nullable String placeholderId,
                               PlayerControlButtonVisibility buttonVisibility,
                               View.OnClickListener onClickListener,
                               @Nullable View.OnLongClickListener longClickListener) {
        ImageView imageView = Utils.getChildViewByResourceName(controlsViewGroup, imageViewButtonId);
        imageView.setVisibility(View.GONE);

        View tempPlaceholder = null;
        if (placeholderId != null) {
            tempPlaceholder = Utils.getChildViewByResourceName(controlsViewGroup, placeholderId);
            tempPlaceholder.setVisibility(View.GONE);
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

    public void setVisibilityImmediate(boolean visible) {
        private_setVisibility(visible, false);
    }

    public void setVisibility(boolean visible, boolean animated) {
        // Ignore this call, otherwise with full screen thumbnails the buttons are visible while seeking.
        if (visible && !animated) return;

        private_setVisibility(visible, animated);
    }

    private void private_setVisibility(boolean visible, boolean animated) {
        try {
            if (isVisible == visible) return;
            isVisible = visible;

            View button = buttonRef.get();
            if (button == null) return;

            View placeholder = placeHolderRef.get();
            final boolean shouldBeShown = visibilityCheck.shouldBeShown();

            if (visible && shouldBeShown) {
                button.clearAnimation();
                if (animated) {
                    button.startAnimation(PlayerControlButton.fadeInAnimation);
                }
                button.setVisibility(View.VISIBLE);

                if (placeholder != null) {
                    placeholder.setVisibility(View.GONE);
                }
            } else {
                if (button.getVisibility() == View.VISIBLE) {
                    button.clearAnimation();
                    if (animated) {
                        button.startAnimation(PlayerControlButton.fadeOutAnimation);
                    }
                    button.setVisibility(View.GONE);
                }

                if (placeholder != null) {
                    placeholder.setVisibility(shouldBeShown
                            ? View.VISIBLE
                            : View.GONE);
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
        if (view != null) view.setVisibility(View.GONE);
        isVisible = false;
    }

    /**
     * Synchronizes the button state after the player state changes.
     * Call this method manually when the player is deployed from a miniplayer.
     */
    public void syncVisibility() {
        Utils.verifyOnMainThread();
        View button = buttonRef.get();
        if (button == null) return;

        View placeholder = placeHolderRef.get();
        Logger.printDebug(() -> "Syncing visibility: isVisible=" + isVisible + ", shouldBeShown=" + visibilityCheck.shouldBeShown());
        button.clearAnimation(); // Очищаємо анімацію

        if (visibilityCheck.shouldBeShown()) {
            if (isVisible) {
                button.setVisibility(View.VISIBLE);
                if (placeholder != null) placeholder.setVisibility(View.GONE);
            } else {
                button.setVisibility(View.GONE);
                if (placeholder != null) placeholder.setVisibility(View.VISIBLE);
            }
        } else {
            button.setVisibility(View.GONE);
            if (placeholder != null) placeholder.setVisibility(View.GONE);
        }
    }
}