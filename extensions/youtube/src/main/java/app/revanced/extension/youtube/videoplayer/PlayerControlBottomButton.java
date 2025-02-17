package app.revanced.extension.youtube.videoplayer;

import static app.revanced.extension.youtube.videoplayer.PlayerControlTopButton.fadeInDuration;
import static app.revanced.extension.youtube.videoplayer.PlayerControlTopButton.fadeOutDuration;

import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;

public abstract class PlayerControlBottomButton {
    private final WeakReference<ImageView> buttonRef;
    private final BooleanSetting setting;
    private boolean isVisible;

    public PlayerControlBottomButton(@NonNull ViewGroup bottomControlsViewGroup, @NonNull String imageViewButtonId,
                                     @NonNull BooleanSetting booleanSetting, @NonNull View.OnClickListener onClickListener,
                                     @Nullable View.OnLongClickListener longClickListener) {
        Logger.printDebug(() -> "Initializing button: " + imageViewButtonId);

        ImageView imageView = Objects.requireNonNull(bottomControlsViewGroup.findViewById(
                Utils.getResourceIdentifier(imageViewButtonId, "id")
        ));
        imageView.setVisibility(View.GONE);

        imageView.setOnClickListener(onClickListener);
        if (longClickListener != null) {
            imageView.setOnLongClickListener(longClickListener);
        }

        setting = booleanSetting;
        buttonRef = new WeakReference<>(imageView);
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
            if (isVisible == visible) return; // If the visibility state hasn't changed, return early
            isVisible = visible;

            ImageView iView = buttonRef.get();
            if (iView == null) {
                return;
            }

            ViewGroup parent = (ViewGroup) iView.getParent();
            if (parent == null) {
                return;
            }

            final Fade fade = new Fade();
            // Use Fade animation with dynamic duration
            fade.setDuration(visible ? fadeInDuration : fadeOutDuration);

            // Apply transition if animation is enabled
            if (animated) {
                TransitionManager.beginDelayedTransition(parent, fade);
            }

            // If the view should be visible and the setting allows it
            if (visible && setting.get()) {
                iView.setVisibility(View.VISIBLE); // Set the view to VISIBLE
            } else if (iView.getVisibility() == View.VISIBLE) {
                iView.setVisibility(View.INVISIBLE); // First, set visibility to INVISIBLE for animation

                // Use Handler to set GONE after the animation is complete
                if (animated) {
                    Utils.runOnMainThreadDelayed(() -> {
                        if (!isVisible) {
                            iView.setVisibility(View.GONE); // Set the view to GONE after the fade animation ends
                        }
                    }, fadeOutDuration); // Delay for the duration of the fade animation
                } else {
                    iView.setVisibility(View.GONE); // If no animation, immediately set the view to GONE
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setVisibility failure", ex);
        }
    }
}
