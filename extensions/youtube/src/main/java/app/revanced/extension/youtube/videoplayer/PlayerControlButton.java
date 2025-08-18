package app.revanced.extension.youtube.videoplayer;

import android.view.View;
import android.view.ViewPropertyAnimator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.shared.PlayerControlsVisibility;
import app.revanced.extension.youtube.shared.PlayerType;
import kotlin.Unit;

public class PlayerControlButton {

    public interface PlayerControlButtonStatus {
        /**
         * @return If the button should be shown when the player overlay is visible.
         */
        boolean buttonEnabled();
    }

    private static final int fadeInDuration = Utils.getResourceInteger("fade_duration_fast");
    private static final int fadeOutDuration = Utils.getResourceInteger("fade_duration_scheduled");

    private final WeakReference<View> containerRef;
    private final WeakReference<View> buttonRef;
    private final WeakReference<TextView> textOverlayRef;
    private final PlayerControlButtonStatus enabledStatus;
    private boolean isVisible;
    private long lastTimeSetVisibile;

    public PlayerControlButton(View controlsViewGroup,
                               String buttonId,
                               @Nullable String textOverlayId,
                               PlayerControlButtonStatus enabledStatus,
                               View.OnClickListener onClickListener,
                               @Nullable View.OnLongClickListener longClickListener) {
        this(controlsViewGroup, buttonId, buttonId, textOverlayId,
                enabledStatus, onClickListener, longClickListener);
    }

    public PlayerControlButton(View controlsViewGroup,
                               String viewToHide,
                               String buttonId,
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

    public void setVisibilityNegatedImmediate() {
        try {
            if (PlayerControlsVisibility.getCurrent() != PlayerControlsVisibility.PLAYER_CONTROLS_VISIBILITY_HIDDEN) {
                return;
            }

            final boolean buttonEnabled = enabledStatus.buttonEnabled();
            if (!buttonEnabled) {
                return;
            }

            View container = containerRef.get();
            if (container == null) {
                return;
            }

            isVisible = false;

            // If the overlay is tapped to display then immediately tapped to dismiss
            // before the fade in animation finishes, then the fade out animation is
            // the time between when the fade in started and now.
            final long animationDuration = Math.min(fadeInDuration,
                    System.currentTimeMillis() - lastTimeSetVisibile);

            ViewPropertyAnimator animate = container.animate();
            animate.cancel();

            if (animationDuration <= 0) {
                // Should never happen, but handle just in case.
                container.setVisibility(View.GONE);
                return;
            }

            animate.alpha(0f)
                    // When overlay is dismissed by tapping,
                    // the duration is the same as when animated to show.
                    .setDuration(animationDuration)
                    .withEndAction(() -> container.setVisibility(View.GONE))
                    .start();
        } catch (Exception ex) {
            Logger.printException(() -> "setVisibilityNegatedImmediate failure", ex);
        }
    }

    public void setVisibilityImmediate(boolean visible) {
        if (visible) {
            // Fix button flickering, by pushing this call to the back of
            // the main thread and letting other layout code run first.
            Utils.runOnMainThread(() -> privateSetVisibility(true, false));
        } else {
            privateSetVisibility(false, false);
        }
    }

    public void setVisibility(boolean visible, boolean animated) {
        // Ignore this call, otherwise with full screen thumbnails the buttons are visible while seeking.
        if (visible && !animated) return;

        privateSetVisibility(visible, animated);
    }

    private void privateSetVisibility(boolean visible, boolean animated) {
        try {
            if (isVisible == visible) return;
            isVisible = visible;

            if (visible) {
                lastTimeSetVisibile = System.currentTimeMillis();
            }

            View container = containerRef.get();
            if (container == null) return;

            final boolean buttonEnabled = enabledStatus.buttonEnabled();

            if (visible && buttonEnabled) {
                container.setVisibility(View.VISIBLE);
                container.setAlpha(animated ? 0f : 1f);
                container.animate()
                        .alpha(1f)
                        .setDuration(animated ? fadeInDuration : 0)
                        .start();
            } else {
                if (container.getVisibility() == View.VISIBLE) {
                    ViewPropertyAnimator animate = container.animate();
                    animate.cancel();
                    animate.alpha(0f)
                            .setDuration(animated ? fadeOutDuration : 0)
                            .withEndAction(() -> container.setVisibility(View.GONE))
                            .start();
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

        container.animate().cancel();

        if (enabledStatus.buttonEnabled() && isVisible) {
            container.setVisibility(View.VISIBLE);
            container.setAlpha(1f);
        } else {
            container.setVisibility(View.GONE);
        }
    }

    public void hide() {
        Utils.verifyOnMainThread();
        if (!isVisible) return;

        View view = containerRef.get();
        if (view == null) return;
        view.setVisibility(View.GONE);
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
