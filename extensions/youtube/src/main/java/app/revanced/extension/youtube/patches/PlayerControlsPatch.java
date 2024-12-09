package app.revanced.extension.youtube.patches;

import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class PlayerControlsPatch {

    /**
     * Injection point.
     */
    public static void setFullscreenCloseButton(ImageView imageButton) {
        // Add a global listener, since the protected method
        // View#onVisibilityChanged() does not have any call backs.
        imageButton.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int lastVisibility = View.VISIBLE;

            @Override
            public void onGlobalLayout() {
                try {
                    final int visibility = imageButton.getVisibility();
                    if (lastVisibility != visibility) {
                        lastVisibility = visibility;

                        Logger.printDebug(() -> "fullscreen button visibility: "
                                + (visibility == View.VISIBLE ? "VISIBLE" :
                                        visibility == View.GONE ? "GONE" : "INVISIBLE"));

                        fullscreenButtonVisibilityChanged(visibility == View.VISIBLE);
                    }
                } catch (Exception ex) {
                    Logger.printDebug(() -> "OnGlobalLayoutListener failure", ex);
                }
            }
        });
    }

    // noinspection EmptyMethod
    public static void fullscreenButtonVisibilityChanged(boolean isVisible) {
        // Code added during patching.
    }

    /**
     * Injection point.
     */
    public static String getPlayerTopControlsLayoutResourceName(String original) {
        return "default";
    }
}
