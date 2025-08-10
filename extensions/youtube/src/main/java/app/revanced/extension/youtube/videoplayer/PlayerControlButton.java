package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.shared.PlayerType;
import kotlin.Unit;

public class PlayerControlButton {

    public interface PlayerControlButtonStatus {
        /**
         * @return If the button should be shown when the player overlay is visible.
         */
        boolean buttonEnabled();
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

    private final WeakReference<View> containerRef;
    private final WeakReference<View> buttonRef;
    /**
     * Empty view with the same layout size as the button. Used to fill empty space while the
     * fade out animation runs. Without this the chapter titles overlapping the button when fading out.
     */
    private final WeakReference<View> placeHolderRef;
    private final WeakReference<TextView> textOverlayRef;
    private final PlayerControlButtonStatus enabledStatus;
    private boolean isVisible;

    public PlayerControlButton(View controlsViewGroup,
                               String buttonId,
                               @Nullable String placeholderId,
                               @Nullable String textOverlayId,
                               PlayerControlButtonStatus enabledStatus,
                               View.OnClickListener onClickListener,
                               @Nullable View.OnLongClickListener longClickListener) {
        this(controlsViewGroup, buttonId, buttonId, placeholderId, textOverlayId,
                enabledStatus, onClickListener, longClickListener);
    }

    public PlayerControlButton(View controlsViewGroup,
                               String viewToHide,
                               String buttonId,
                               @Nullable String placeholderId,
                               @Nullable String textOverlayId,
                               PlayerControlButtonStatus enabledStatus,
                               View.OnClickListener onClickListener,
                               @Nullable View.OnLongClickListener longClickListener) {
        View containerView = Utils.getChildViewByResourceName(controlsViewGroup, viewToHide);
        containerView.setVisibility(View.GONE);
        containerRef = new WeakReference<>(containerView);

        View button = Utils.getChildViewByResourceName(controlsViewGroup, buttonId);
        button.setOnClickListener(onClickListener);
        if (longClickListener != null) {
            button.setOnLongClickListener(longClickListener);
        }
        buttonRef = new WeakReference<>(button);

        View tempPlaceholder = null;
        if (placeholderId != null) {
            tempPlaceholder = Utils.getChildViewByResourceName(controlsViewGroup, placeholderId);
            tempPlaceholder.setVisibility(View.GONE);
        }
        placeHolderRef = new WeakReference<>(tempPlaceholder);

        TextView tempTextOverlay = null;
        if (textOverlayId != null) {
            tempTextOverlay = Utils.getChildViewByResourceName(controlsViewGroup, textOverlayId);
        }
        textOverlayRef = new WeakReference<>(tempTextOverlay);

        this.enabledStatus = enabledStatus;
        isVisible = false;

        // Update the visibility after the player type changes.
        // This ensures that button animations are cleared and their states are updated correctly
        // when switching between states like minimized, maximized, or fullscreen, preventing
        // "stuck" animations or incorrect visibility.  Without this fix the issue is most noticeable
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

            View container = containerRef.get();
            if (container == null) return;

            View placeholder = placeHolderRef.get();
            final boolean buttonEnabled = enabledStatus.buttonEnabled();

            if (visible && buttonEnabled) {
                container.clearAnimation();
                if (animated) {
                    container.startAnimation(fadeInAnimation);
                }
                container.setVisibility(View.VISIBLE);

                if (placeholder != null) {
                    placeholder.setVisibility(View.GONE);
                }
            } else {
                if (container.getVisibility() == View.VISIBLE) {
                    container.clearAnimation();
                    if (animated) {
                        container.startAnimation(fadeOutAnimation);
                    }
                    container.setVisibility(View.GONE);
                }

                if (placeholder != null) {
                    placeholder.setVisibility(buttonEnabled
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

        View container = containerRef.get();
        if (container == null) return;

        container.clearAnimation();
        View placeholder = placeHolderRef.get();

        if (enabledStatus.buttonEnabled()) {
            if (isVisible) {
                container.setVisibility(View.VISIBLE);
                if (placeholder != null) placeholder.setVisibility(View.GONE);
            } else {
                container.setVisibility(View.GONE);
                if (placeholder != null) placeholder.setVisibility(View.VISIBLE);
            }
        } else {
            container.setVisibility(View.GONE);
            if (placeholder != null) placeholder.setVisibility(View.GONE);
        }
    }

    public void hide() {
        Utils.verifyOnMainThread();
        if (!isVisible) return;

        View view = containerRef.get();
        if (view == null) return;
        view.setVisibility(View.GONE);

        View placeHolder = placeHolderRef.get();
        if (placeHolder != null) view.setVisibility(View.GONE);

        isVisible = false;
    }

    /**
     * Sets the icon of the button.
     * @param resourceId Drawable identifier, or zero to hide the icon.
     */
    public void setIcon(int resourceId) {
        View button = buttonRef.get();
        if (button instanceof ImageView imageButton) {
            imageButton.setImageResource(resourceId);
        }
    }

    /**
     * Sets the text to be displayed on the text overlay.
     * @param text The text to set on the overlay, or null to clear the text.
     */
    public void setTextOverlay(CharSequence text) {
        TextView textOverlay = textOverlayRef.get();
        if (textOverlay != null) {
            textOverlay.setText(text);
        }
    }
}
