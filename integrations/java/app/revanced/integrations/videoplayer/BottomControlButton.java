package app.revanced.integrations.videoplayer;

import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

import java.lang.ref.WeakReference;
import java.util.Objects;

public abstract class BottomControlButton {
    private static final Animation fadeIn = ReVancedUtils.getResourceAnimation("fade_in");
    private static final Animation fadeOut = ReVancedUtils.getResourceAnimation("fade_out");
    private final WeakReference<ImageView> buttonRef;
    private final SettingsEnum setting;
    protected boolean isVisible;

    static {
        // TODO: check if these durations are correct.
        fadeIn.setDuration(ReVancedUtils.getResourceInteger("fade_duration_fast"));
        fadeOut.setDuration(ReVancedUtils.getResourceInteger("fade_duration_scheduled"));
    }

    public BottomControlButton(@NonNull ViewGroup bottomControlsViewGroup, @NonNull String imageViewButtonId,
                               @NonNull SettingsEnum booleanSetting, @NonNull View.OnClickListener onClickListener) {
        LogHelper.printDebug(() -> "Initializing button: " + imageViewButtonId);

        if (booleanSetting.returnType != SettingsEnum.ReturnType.BOOLEAN) {
            throw new IllegalArgumentException();
        }

        setting = booleanSetting;

        // Create the button.
        ImageView imageView = Objects.requireNonNull(bottomControlsViewGroup.findViewById(
                ReVancedUtils.getResourceIdentifier(imageViewButtonId, "id")
        ));
        imageView.setOnClickListener(onClickListener);
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
        if (visible && setting.getBoolean()) {
            imageView.startAnimation(fadeIn);
            imageView.setVisibility(View.VISIBLE);
        } else if (imageView.getVisibility() == View.VISIBLE) {
            imageView.startAnimation(fadeOut);
            imageView.setVisibility(View.GONE);
        }
    }
}
