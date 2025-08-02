package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.shared.PlayerType;
import kotlin.Unit;

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

        // Update the visibility after the player type changes.
        // This ensures that button animations are cleared and their states are updated correctly
        // when switching between states like minimized, maximized, or fullscreen, preventing
        // "stuck" animations or incorrect visibility.  Without this fix the issue is most noticable
        // when maximizing type 3 miniplayer.
        PlayerType.getOnChange().addObserver((PlayerType type) -> {
            playerTypeChanged(type);
            return Unit.INSTANCE;
        });
    }

    public void setVisibilityImmediate(boolean visible) {
        if (visible) {
            // Fix button flickering, by pushing this call to the back of
            // the main thread and letting other layout code run first.
            Utils.runOnMainThread(() -> private_setVisibility(true, false));
        } else {
            private_setVisibility(false, false);
        }
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

    /**
     * Synchronizes the button state after the player state changes.
     */
    private void playerTypeChanged(PlayerType newType) {
        if (newType != PlayerType.WATCH_WHILE_MINIMIZED && !newType.isMaximizedOrFullscreen()) {
            return;
        }

        View button = buttonRef.get();
        if (button == null) return;

        button.clearAnimation();
        View placeholder = placeHolderRef.get();

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
     * Sets the icon of the button.
     * @param resourceId Drawable identifier, or zero to hide the icon.
     */
    public void setIcon(int resourceId) {
        try {
            View button = buttonRef.get();
            if (button instanceof ImageView imageButton) {
                imageButton.setImageResource(resourceId);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setIcon failure", ex);
        }
    }

    /**
     * Starts an animation on the button.
     * @param animation The animation to apply.
     */
    public void startAnimation(Animation animation) {
        try {
            View button = buttonRef.get();
            if (button != null) {
                button.startAnimation(animation);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "startAnimation failure", ex);
        }
    }

    /**
     * Clears any animation on the button.
     */
    public void clearAnimation() {
        try {
            View button = buttonRef.get();
            if (button != null) {
                button.clearAnimation();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "clearAnimation failure", ex);
        }
    }

    /**
     * Returns the View associated with this button.
     * @return The button View.
     */
    public View getView() {
        return buttonRef.get();
    }
}
