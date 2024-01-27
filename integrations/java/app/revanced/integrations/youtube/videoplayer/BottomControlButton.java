package app.revanced.integrations.youtube.videoplayer;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Objects;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.shared.settings.BooleanSetting;

public abstract class BottomControlButton {
    private static final Animation fadeIn;
    private static final Animation fadeOut;

    private final WeakReference<ImageView> buttonRef;
    private final BooleanSetting setting;
    protected boolean isVisible;

    static {
        // TODO: check if these durations are correct.
        fadeIn = Utils.getResourceAnimation("fade_in");
        fadeIn.setDuration(Utils.getResourceInteger("fade_duration_fast"));

        fadeOut = Utils.getResourceAnimation("fade_out");
        fadeOut.setDuration(Utils.getResourceInteger("fade_duration_scheduled"));
    }

    @NonNull
    public static Animation getButtonFadeIn() {
        return fadeIn;
    }

    @NonNull
    public static Animation getButtonFadeOut() {
        return fadeOut;
    }

    public BottomControlButton(@NonNull ViewGroup bottomControlsViewGroup, @NonNull String imageViewButtonId,
                               @NonNull BooleanSetting booleanSetting, @NonNull View.OnClickListener onClickListener,
                               @Nullable View.OnLongClickListener longClickListener) {
        Logger.printDebug(() -> "Initializing button: " + imageViewButtonId);

        setting = booleanSetting;

        // Create the button.
        ImageView imageView = Objects.requireNonNull(bottomControlsViewGroup.findViewById(
                Utils.getResourceIdentifier(imageViewButtonId, "id")
        ));
        imageView.setOnClickListener(onClickListener);
        if (longClickListener != null) {
            imageView.setOnLongClickListener(longClickListener);
        }
        imageView.setVisibility(View.GONE);

        buttonRef = new WeakReference<>(imageView);
    }

    public void setVisibility(boolean visible) {
        if (isVisible == visible) return;
        isVisible = visible;

        ImageView imageView = buttonRef.get();
        if (imageView == null) {
            return;
        }

        imageView.clearAnimation();
        if (visible && setting.get()) {
            imageView.startAnimation(fadeIn);
            imageView.setVisibility(View.VISIBLE);
        } else if (imageView.getVisibility() == View.VISIBLE) {
            imageView.startAnimation(fadeOut);
            imageView.setVisibility(View.GONE);
        }
    }
}
