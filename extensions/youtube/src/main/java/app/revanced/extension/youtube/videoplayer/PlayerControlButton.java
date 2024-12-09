package app.revanced.extension.youtube.videoplayer;

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
            isVisible = visible;

            ImageView iView = buttonRef.get();
            if (iView == null) {
                return;
            }

            if (visible && setting.get()) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(PlayerControlButton.getButtonFadeIn());
                }
                iView.setVisibility(View.VISIBLE);
            } else if (iView.getVisibility() == View.VISIBLE) {
                iView.clearAnimation();
                if (animated) {
                    iView.startAnimation(PlayerControlButton.getButtonFadeOut());
                }
                iView.setVisibility(View.GONE);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "setVisibility failure", ex);
        }
    }
}
