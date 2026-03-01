package app.revanced.extension.youtube.patches;

import static app.revanced.extension.shared.Utils.getResourceIdentifierOrThrow;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.ResourceType;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class HidePlayerOverlayButtonsPatch {

    private static final boolean HIDE_AUTOPLAY_BUTTON_ENABLED = Settings.HIDE_AUTOPLAY_BUTTON.get();

    /**
     * Injection point.
     */
    public static boolean hideAutoplayButton() {
        return HIDE_AUTOPLAY_BUTTON_ENABLED;
    }

    /**
     * Injection point.
     */
    public static int getCastButtonOverrideV2(int original) {
        return Settings.HIDE_CAST_BUTTON.get() ? View.GONE : original;
    }

    /**
     * Injection point.
     */
    public static boolean getCastButtonOverrideV2(boolean original) {
        if (Settings.HIDE_CAST_BUTTON.get()) return false;

        return original;
    }

    /**
     * Injection point.
     */
    public static void hideCaptionsButton(ImageView imageView) {
        imageView.setVisibility(Settings.HIDE_CAPTIONS_BUTTON.get() ? ImageView.GONE : ImageView.VISIBLE);
    }

    /**
     * Injection point.
     */
    public static void hideCollapseButton(ImageView imageView) {
        if (!Settings.HIDE_COLLAPSE_BUTTON.get()) return;

        // Make the collapse button invisible
        imageView.setImageResource(android.R.color.transparent);
        imageView.setImageAlpha(0);
        imageView.setEnabled(false);

        // Adjust layout params if RelativeLayout
        var layoutParams = imageView.getLayoutParams();
        if (layoutParams instanceof android.widget.RelativeLayout.LayoutParams) {
            android.widget.RelativeLayout.LayoutParams lp = new android.widget.RelativeLayout.LayoutParams(0, 0);
            imageView.setLayoutParams(lp);
        } else {
            Logger.printDebug(() -> "Unknown collapse button layout params: " + layoutParams);
        }
    }

    /**
     * Injection point.
     */
    public static void setTitleAnchorStartMargin(View titleAnchorView) {
        if (!Settings.HIDE_COLLAPSE_BUTTON.get()) return;

        var layoutParams = titleAnchorView.getLayoutParams();
        if (layoutParams instanceof android.widget.RelativeLayout.LayoutParams relativeParams) {
            relativeParams.setMarginStart(0);
        } else {
            Logger.printDebug(() -> "Unknown title anchor layout params: " + layoutParams);
        }
    }

    private static final boolean HIDE_PLAYER_PREVIOUS_NEXT_BUTTONS_ENABLED
            = Settings.HIDE_PLAYER_PREVIOUS_NEXT_BUTTONS.get();

    private static final int PLAYER_CONTROL_PREVIOUS_BUTTON_TOUCH_AREA_ID = getResourceIdentifierOrThrow(
            ResourceType.ID, "player_control_previous_button_touch_area");

    private static final int PLAYER_CONTROL_NEXT_BUTTON_TOUCH_AREA_ID = getResourceIdentifierOrThrow(
            ResourceType.ID, "player_control_next_button_touch_area");

    /**
     * Injection point.
     */
    public static void hidePreviousNextButtons(View parentView) {
        if (!HIDE_PLAYER_PREVIOUS_NEXT_BUTTONS_ENABLED) {
            return;
        }

        // Must use a deferred call to main thread to hide the button.
        // Otherwise, the layout crashes if set to hidden now.
        Utils.runOnMainThread(() -> {
            hideView(parentView, PLAYER_CONTROL_PREVIOUS_BUTTON_TOUCH_AREA_ID);
            hideView(parentView, PLAYER_CONTROL_NEXT_BUTTON_TOUCH_AREA_ID);
        });
    }

    /**
     * Injection point.
     */
    public static ImageView hideFullscreenButton(ImageView imageView) {
        if (!Settings.HIDE_FULLSCREEN_BUTTON.get()) {
            return imageView;
        }

        if (imageView != null) {
            imageView.setVisibility(View.GONE);
        }

        return null;
    }

    /**
     * Injection point.
     */
    public static void hidePlayerControlButtonsBackground(View rootView) {
        try {
            if (!Settings.HIDE_PLAYER_CONTROL_BUTTONS_BACKGROUND.get()) {
                return;
            }

            // Each button is an ImageView with a background set to another drawable.
            removeImageViewsBackgroundRecursive(rootView);
        } catch (Exception ex) {
            Logger.printException(() -> "removePlayerControlButtonsBackground failure", ex);
        }
    }

    private static void hideView(View parentView, int resourceId) {
        View nextPreviousButton = parentView.findViewById(resourceId);

        if (nextPreviousButton == null) {
            Logger.printException(() -> "Could not find player previous/next button");
            return;
        }

        Logger.printDebug(() -> "Hiding previous/next button");
        Utils.hideViewByRemovingFromParentUnderCondition(true, nextPreviousButton);
    }

    private static void removeImageViewsBackgroundRecursive(View currentView) {
        if (currentView instanceof ImageView imageView) {
            imageView.setBackground(null);
        }

        if (currentView instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                removeImageViewsBackgroundRecursive(viewGroup.getChildAt(i));
            }
        }
    }
}
