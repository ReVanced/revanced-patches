package app.revanced.extension.youtube.patches;

import android.view.View;
import android.view.ViewTreeObserver;

import java.lang.ref.WeakReference;

import app.revanced.extension.shared.Logger;

@SuppressWarnings("unused")
public class PlayerControlsPatch {

    public static WeakReference<View> fullscreenButtonRef = new WeakReference<>(null);

    private static boolean fullscreenButtonVisibilityCallbacksExist() {
        return false; // Modified during patching if needed.
    }

    /**
     * Injection point.
     */
    public static void setFullscreenCloseButton(View button) {
        fullscreenButtonRef = new WeakReference<>(button);
        Logger.printDebug(() -> "Fullscreen button set");

        if (!fullscreenButtonVisibilityCallbacksExist()) {
            return;
        }

        // Add a global listener, since the protected method
        // View#onVisibilityChanged() does not have any call backs.
        button.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            int lastVisibility = View.VISIBLE;

            @Override
            public void onGlobalLayout() {
                try {
                    final int visibility = button.getVisibility();
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
    private static void fullscreenButtonVisibilityChanged(boolean isVisible) {
        // Code added during patching.
    }
}
