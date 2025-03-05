package app.revanced.extension.youtube.patches;

import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton;

import android.view.View;

import androidx.annotation.Nullable;

import java.util.Objects;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.youtube.settings.Settings;
import app.revanced.extension.youtube.shared.NavigationBar;
import app.revanced.extension.youtube.shared.PlayerType;

@SuppressWarnings("unused")
public class ChangeFormFactorPatch {

    public enum FormFactor {
        /**
         * Unmodified, and same as un-patched.
         */
        DEFAULT(null),
        /**
         * <pre>
         * Some changes include:
         * - Explore tab is present.
         * - watch history is missing.
         * - feed thumbnails fade in.
         */
        UNKNOWN(0),
        SMALL(1),
        LARGE(2),
        /**
         * Cars with 'Google built-in'.
         * Layout seems identical to {@link #UNKNOWN}
         * even when using an Android Automotive device.
         */
        AUTOMOTIVE(3),
        WEARABLE(4);

        @Nullable
        final Integer formFactorType;

        FormFactor(@Nullable Integer formFactorType) {
            this.formFactorType = formFactorType;
        }
    }

    @Nullable
    private static final Integer FORM_FACTOR_TYPE = Settings.CHANGE_FORM_FACTOR.get().formFactorType;
    private static final boolean USING_AUTOMOTIVE_TYPE = Objects.requireNonNull(
            FormFactor.AUTOMOTIVE.formFactorType).equals(FORM_FACTOR_TYPE);

    /**
     * Injection point.
     */
    public static int getFormFactor(int original) {
        if (FORM_FACTOR_TYPE == null) return original;

        if (USING_AUTOMOTIVE_TYPE) {
            // Do not change if the player is opening or is opened,
            // otherwise the video description cannot be opened.
            PlayerType current = PlayerType.getCurrent();
            if (current.isMaximizedOrFullscreen() || current == PlayerType.WATCH_WHILE_SLIDING_MINIMIZED_MAXIMIZED) {
                Logger.printDebug(() -> "Using original form factor for player");
                return original;
            }

            if (!NavigationBar.isSearchBarActive()) {
                // Automotive type shows error 400 when opening a channel page and using some explore tab.
                // This is a bug in unpatched YouTube that occurs on actual Android Automotive devices.
                // Work around the issue by using the original form factor if not in search and the
                // navigation back button is present.
                if (NavigationBar.isBackButtonVisible()) {
                    Logger.printDebug(() -> "Using original form factor, as back button is visible without search present");
                    return original;
                }

                // Do not change library tab otherwise watch history is hidden.
                // Do this check last since the current navigation button is required.
                if (NavigationButton.getSelectedNavigationButton() == NavigationButton.LIBRARY) {
                    return original;
                }
            }
        }

        return FORM_FACTOR_TYPE;
    }

    /**
     * Injection point.
     */
    public static void navigationTabCreated(NavigationButton button, View tabView) {
        // On first startup of the app the navigation buttons are fetched and updated.
        // If the user immediately opens the 'You' or opens a video, then the call to
        // update the navigtation buttons will use the non automotive form factor
        // and the explore tab is missing.
        // Fixing this is not so simple because of the concurrent calls for the player and You tab.
        // For now, always hide the explore tab.
        if (USING_AUTOMOTIVE_TYPE && button == NavigationButton.EXPLORE) {
            tabView.setVisibility(View.GONE);
        }
    }
}