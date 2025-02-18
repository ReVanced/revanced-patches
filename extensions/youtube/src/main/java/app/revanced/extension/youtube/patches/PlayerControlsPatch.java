package app.revanced.extension.youtube.patches;

import android.view.View;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.videoplayer.HookedTouchImageView;

@SuppressWarnings("unused")
public class PlayerControlsPatch {

    public static WeakReference<ImageView> fullscreenButtonRef = new WeakReference<>(null);

    private static boolean fullscreenButtonVisibilityCallbacksExist() {
        return false; // Modified during patching if needed.
    }

    /**
     * Injection point.
     */
    public static void setFullscreenCloseButton(ImageView view) {
        try {
            HookedTouchImageView imageButton = (HookedTouchImageView) view;
            fullscreenButtonRef = new WeakReference<>(imageButton);
            Logger.printDebug(() -> "Fullscreen button set");

            if (!fullscreenButtonVisibilityCallbacksExist()) {
                return;
            }

            imageButton.setVisibilityChangeListener((listenerView, visibility) -> {
                Logger.printDebug(() -> "fullscreen button visibility: "
                        + (visibility == View.VISIBLE ? "VISIBLE" :
                        visibility == View.GONE ? "GONE" : "INVISIBLE"));

                fullscreenButtonVisibilityChanged(visibility == View.VISIBLE);
            });
        } catch (Exception ex) {
            Logger.printException(() -> "setFullscreenCloseButton failure", ex);
        }
    }

    // noinspection EmptyMethod
    private static void fullscreenButtonVisibilityChanged(boolean isVisible) {
        // Code added during patching.
    }

    /**
     * Injection point.
     */
    public static String getPlayerTopControlsLayoutResourceName(String original) {
        return "default";
    }
}
