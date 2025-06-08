package app.revanced.extension.youtube.patches;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unused")
public class RemovePlayerControlsBackgroundPatch {

    private static List<ImageView> findImageViews(View rootView) {
        List<ImageView> foundViews = new ArrayList<>();
        findImageViewsRecursive(rootView, foundViews);
        return foundViews;
    }

    private static void findImageViewsRecursive(View currentView, List<ImageView> foundViews) {
        if (currentView instanceof ImageView imageView) {
            foundViews.add(imageView);
        }

        if (currentView instanceof ViewGroup viewGroup) {
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                findImageViewsRecursive(viewGroup.getChildAt(i), foundViews);
            }
        }
    }

    /**
     * Injection point.
     */
    public static void removePlayerControlButtonsBackground(View rootView) {
        try {
            if (!Settings.REMOVE_PLAYER_CONTROL_BUTTONS_BACKGROUND.get()) {
                return;
            }

            // Each button is an ImageView with a background set to another drawable.
            List<ImageView> imageViews = findImageViews(rootView);

            for (ImageView imageView : imageViews) {
                imageView.setBackground(null);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "removePlayerControlButtonsBackground failure", ex);
        }
    }
}
