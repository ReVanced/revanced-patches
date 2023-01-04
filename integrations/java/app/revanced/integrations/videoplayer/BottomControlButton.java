package app.revanced.integrations.videoplayer;

import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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

            ImageView imageView = constraintLayout.findViewById(ReVancedUtils.getIdentifier(viewId, "id"));
            if (imageView == null) {
                LogHelper.printDebug(() -> "Couldn't find ImageView with id: " + viewId);
                return;
            }

            imageView.setOnClickListener(onClickListener);

            button = new WeakReference<>(imageView);
            fadeIn = getAnimation("fade_in");
            fadeOut = getAnimation("fade_out");

            int fadeDurationFast = getInteger("fade_duration_fast");
            int fadeDurationScheduled = getInteger("fade_duration_scheduled");
            fadeIn.setDuration(fadeDurationFast);
            fadeOut.setDuration(fadeDurationScheduled);
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
    private static int getInteger(String str) {
        return ReVancedUtils.getContext().getResources().getInteger(ReVancedUtils.getIdentifier(str, "integer"));
    }

    private static Animation getAnimation(String str) {
        return AnimationUtils.loadAnimation(ReVancedUtils.getContext(), ReVancedUtils.getIdentifier(str, "anim"));
    }
}
