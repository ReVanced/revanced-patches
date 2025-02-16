package app.revanced.extension.youtube.videoplayer;

import android.os.Handler;
import android.os.Looper;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BooleanSetting;

public abstract class PlayerControlButton {
    private static final Animation fadeIn;
    private static final Animation fadeOut;
    private static final Animation fadeOutImmediate;

    private final WeakReference<ImageView> buttonRef;
    protected final BooleanSetting setting;
    protected boolean isVisible;

    static {
        // TODO: check if these durations are correct.
        fadeIn = Utils.getResourceAnimation("fade_in");
        fadeIn.setDuration(Utils.getResourceInteger("fade_duration_fast"));

        fadeOut = Utils.getResourceAnimation("fade_out");
        fadeOut.setDuration(Utils.getResourceInteger("fade_duration_scheduled"));

        fadeOutImmediate = Utils.getResourceAnimation("abc_fade_out");
        fadeOutImmediate.setDuration(Utils.getResourceInteger("fade_duration_fast"));
    }

    @NonNull
    public static Animation getButtonFadeIn() {
        return fadeIn;
    }

    @NonNull
    public static Animation getButtonFadeOut() {
        return fadeOut;
    }

    @NonNull
    public static Animation getButtonFadeOutImmediately() {
        return fadeOutImmediate;
    }

    public PlayerControlButton(@NonNull ViewGroup bottomControlsViewGroup, @NonNull String imageViewButtonId,
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
            isVisible = visible; // If the visibility state hasn't changed, return early

            ImageView iView = buttonRef.get();
            if (iView == null) {
                return; // Return if the ImageView is null
            }

            ViewGroup parent = (ViewGroup) iView.getParent();
            if (parent == null) {
                return; // Return if the parent view is null
            }

            // Use Fade animation with dynamic duration
            Fade fade = new Fade();
            fade.setDuration(visible ? Utils.getResourceInteger("fade_duration_fast")
                                     : Utils.getResourceInteger("fade_duration_scheduled"));

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
                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        if (!isVisible) {
                            iView.setVisibility(View.GONE); // Set the view to GONE after the fade animation ends
                        }
                    }, fade.getDuration()); // Delay for the duration of the fade animation
                } else {
                    iView.setVisibility(View.GONE); // If no animation, immediately set the view to GONE
                }
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setVisibility failure", ex);
        }
    }
}
