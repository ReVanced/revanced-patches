package app.revanced.integrations.youtube.shared;

import static app.revanced.integrations.youtube.shared.NavigationBar.NavigationButton.CREATE;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.Utils;
import app.revanced.integrations.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class NavigationBar {

    private static volatile WeakReference<View> searchBarResultsRef = new WeakReference<>(null);

    /**
     * Injection point.
     */
    public static void searchBarResultsViewLoaded(View searchbarResults) {
        searchBarResultsRef = new WeakReference<>(searchbarResults);
    }

    /**
     * @return If the search bar is on screen.  This includes if the player
     *         is on screen and the search results are behind the player (and not visible).
     *         Detecting the search is covered by the player can be done by checking {@link PlayerType#isMaximizedOrFullscreen()}.
     */
    public static boolean isSearchBarActive() {
        View searchbarResults = searchBarResultsRef.get();
        return searchbarResults != null && searchbarResults.getParent() != null;
    }

    /**
     * Last YT navigation enum loaded.  Not necessarily the active navigation tab.
     */
    @Nullable
    private static volatile String lastYTNavigationEnumName;

    /**
     * Injection point.
     */
    public static void setLastAppNavigationEnum(@Nullable Enum<?> ytNavigationEnumName) {
        if (ytNavigationEnumName != null) {
            lastYTNavigationEnumName = ytNavigationEnumName.name();
        }
    }

    /**
     * Injection point.
     */
    public static void navigationTabLoaded(final View navigationButtonGroup) {
        try {
            String lastEnumName = lastYTNavigationEnumName;
            for (NavigationButton button : NavigationButton.values()) {
                if (button.ytEnumName.equals(lastEnumName)) {
                    ImageView imageView = Utils.getChildView((ViewGroup) navigationButtonGroup,
                            true, view -> view instanceof ImageView);

                    if (imageView != null) {
                        Logger.printDebug(() -> "navigationTabLoaded: " + lastEnumName);

                        button.imageViewRef = new WeakReference<>(imageView);
                        navigationTabCreatedCallback(button, navigationButtonGroup);

                        return;
                    }
                }
            }
            // Log the unknown tab as exception level, only if debug is enabled.
            // This is because unknown tabs do no harm, and it's only relevant to developers.
            if (Settings.DEBUG.get()) {
                Logger.printException(() -> "Unknown tab: " + lastEnumName
                        + " view: " + navigationButtonGroup.getClass());
            }
        } catch (Exception ex) {
            Logger.printException(() -> "navigationTabLoaded failure", ex);
        }
    }

    /**
     * Injection point.
     *
     * Unique hook just for the 'Create' and 'You' tab.
     */
    public static void navigationImageResourceTabLoaded(View view) {
        // 'You' tab has no YT enum name and the enum hook is not called for it.
        // Compare the last enum to figure out which tab this actually is.
        if (CREATE.ytEnumName.equals(lastYTNavigationEnumName)) {
            navigationTabLoaded(view);
        } else {
            lastYTNavigationEnumName = NavigationButton.LIBRARY_YOU.ytEnumName;
            navigationTabLoaded(view);
        }
    }

    /** @noinspection EmptyMethod*/
    private static void navigationTabCreatedCallback(NavigationButton button, View tabView) {
        // Code is added during patching.
    }

    public enum NavigationButton {
        HOME("PIVOT_HOME"),
        SHORTS("TAB_SHORTS"),
        /**
         * Create new video tab.
         *
         * {@link #isSelected()} always returns false, even if the create video UI is on screen.
         */
        CREATE("CREATION_TAB_LARGE"),
        SUBSCRIPTIONS("PIVOT_SUBSCRIPTIONS"),
        /**
         * Notifications tab.  Only present when
         * {@link Settings#SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON} is active.
         */
        NOTIFICATIONS("TAB_ACTIVITY"),
        /**
         * Library tab when the user is not logged in.
         */
        LIBRARY_LOGGED_OUT("ACCOUNT_CIRCLE"),
        /**
         * User is logged in with incognito mode enabled.
         */
        LIBRARY_INCOGNITO("INCOGNITO_CIRCLE"),
        /**
         * Old library tab (pre 'You' layout), only present when version spoofing.
         */
        LIBRARY_OLD_UI("VIDEO_LIBRARY_WHITE"),
        /**
         * 'You' library tab that is sometimes momentarily loaded.
         * When this is loaded, {@link #LIBRARY_YOU} is also present.
         *
         * This might be a temporary tab while the user profile photo is loading,
         * but its exact purpose is not entirely clear.
         */
        LIBRARY_PIVOT_UNKNOWN("PIVOT_LIBRARY"),
        /**
         * Modern library tab with 'You' layout.
         */
        // The hooked YT code does not use an enum, and a dummy name is used here.
        LIBRARY_YOU("YOU_LIBRARY_DUMMY_PLACEHOLDER_NAME");

        /**
         * @return The active navigation tab.
         *         If the user is in the create new video UI, this returns NULL.
         */
        @Nullable
        public static NavigationButton getSelectedNavigationButton() {
            for (NavigationButton button : values()) {
                if (button.isSelected()) return button;
            }
            return null;
        }

        /**
         * @return If the currently selected tab is a 'You' or library type.
         *         Covers all known app states including incognito mode and version spoofing.
         */
        public static boolean libraryOrYouTabIsSelected() {
            return LIBRARY_YOU.isSelected() || LIBRARY_PIVOT_UNKNOWN.isSelected()
                    || LIBRARY_OLD_UI.isSelected() || LIBRARY_INCOGNITO.isSelected()
                    || LIBRARY_LOGGED_OUT.isSelected();
        }

        /**
         * YouTube enum name for this tab.
         */
        private final String ytEnumName;
        private volatile WeakReference<ImageView> imageViewRef = new WeakReference<>(null);

        NavigationButton(String ytEnumName) {
            this.ytEnumName = ytEnumName;
        }

        public boolean isSelected() {
            ImageView view = imageViewRef.get();
            return view != null && view.isSelected();
        }
    }
}
