package app.revanced.integrations.videoplayer;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import app.revanced.integrations.utils.LogHelper;
import app.revanced.integrations.utils.ReVancedUtils;

public abstract class BottomControlButton {
    WeakReference<ImageView> button = new WeakReference<>(null);
    ConstraintLayout constraintLayout;
    Boolean isButtonEnabled;
    Boolean isShowing;

    private Animation fadeIn;
    private Animation fadeOut;

    public BottomControlButton(Object obj, String viewId, Boolean isEnabled, View.OnClickListener onClickListener) {
        try {
            LogHelper.printDebug(() -> "Initializing button with id: " + viewId);
            constraintLayout = (ConstraintLayout) obj;
            isButtonEnabled = isEnabled;

            ImageView imageView = constraintLayout.findViewById(ReVancedUtils.getResourceIdentifier(viewId, "id"));
            if (imageView == null) {
                LogHelper.printException(() -> "Couldn't find ImageView with id: " + viewId);
                return;
            }
            imageView.setOnClickListener(onClickListener);
            button = new WeakReference<>(imageView);

            fadeIn = ReVancedUtils.getResourceAnimation("fade_in");
            fadeOut = ReVancedUtils.getResourceAnimation("fade_out");
            fadeIn.setDuration(ReVancedUtils.getResourceInteger("fade_duration_fast"));
            fadeOut.setDuration(ReVancedUtils.getResourceInteger("fade_duration_scheduled"));

            isShowing = true;
            setVisibility(false);
        } catch (Exception e) {
            LogHelper.printException(() -> "Failed to initialize button with id: " + viewId, e);
        }
    }

    public void setVisibility(boolean showing) {
        if (isShowing == showing) return;

        isShowing = showing;
        ImageView imageView = button.get();

        if (constraintLayout == null || imageView == null)
            return;

        if (showing && isButtonEnabled) {
            LogHelper.printDebug(() -> "Fading in");
            imageView.setVisibility(View.VISIBLE);
            imageView.startAnimation(fadeIn);
        }
        else if (imageView.getVisibility() == View.VISIBLE) {
            LogHelper.printDebug(() -> "Fading out");
            imageView.startAnimation(fadeOut);
            imageView.setVisibility(View.GONE);
        }
    }
}
