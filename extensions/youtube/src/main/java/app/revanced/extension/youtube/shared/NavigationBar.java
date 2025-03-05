package app.revanced.extension.youtube.shared;

import static app.revanced.extension.youtube.shared.NavigationBar.NavigationButton.CREATE;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.youtube.settings.Settings;

@SuppressWarnings("unused")
public final class NavigationBar {

    /**
     * Interface to call obfuscated methods in AppCompat Toolbar class.
     */
    public interface AppCompatToolbarPatchInterface {
        Drawable patch_getNavigationIcon();
    }

    //
    // Search and toolbar.
    //

    private static volatile WeakReference<View> searchBarResultsRef = new WeakReference<>(null);

    private static volatile WeakReference<AppCompatToolbarPatchInterface> toolbarResultsRef
            = new WeakReference<>(null);

    /**
     * Injection point.
     */
    public static void searchBarResultsViewLoaded(View searchbarResults) {
        searchBarResultsRef = new WeakReference<>(searchbarResults);
    }

    /**
     * Injection point.
     */
    public static void setToolbar(FrameLayout layout) {
        AppCompatToolbarPatchInterface toolbar = Utils.getChildView(layout, false, (view) ->
                view instanceof AppCompatToolbarPatchInterface
        );

        if (toolbar == null) {
            Logger.printException(() -> "Could not find navigation toolbar");
            return;
        }

        toolbarResultsRef = new WeakReference<>(toolbar);
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

    public static boolean isBackButtonVisible() {
        AppCompatToolbarPatchInterface toolbar = toolbarResultsRef.get();
        return toolbar != null && toolbar.patch_getNavigationIcon() != null;
    }

    //
    // Navigation bar buttons.
    //

    /**
     * How long to wait for the set nav button latch to be released.  Maximum wait time must
     * be as small as possible while still allowing enough time for the nav bar to update.
     *
     * YT calls it's back button handlers out of order, and litho starts filtering before the
     * navigation bar is updated. Fixing this situation and not needlessly waiting requires
     * somehow detecting if a back button key/gesture will not change the active tab.
     *
     * On average the time between pressing the back button and the first litho event is
     * about 10-20ms.  Waiting up to 75-150ms should be enough time to handle normal use cases
     * and not be noticeable, since YT typically takes 100-200ms (or more) to update the view.
     *
     * This delay is only noticeable when the device back button/gesture will not
     * change the current navigation tab, such as backing out of the watch history.
     *
     * This issue can also be avoided on a patch by patch basis, by avoiding calls to
     * {@link NavigationButton#getSelectedNavigationButton()} unless absolutely necessary.
     */
    private static final long LATCH_AWAIT_TIMEOUT_MILLISECONDS = 120;

    /**
     * Used as a workaround to fix the issue of YT calling back button handlers out of order.
     * Used to hold calls to {@link NavigationButton#getSelectedNavigationButton()}
     * until the current navigation button can be determined.
     *
     * Only used when the hardware back button is pressed.
     */
    @Nullable
    private static volatile CountDownLatch navButtonLatch;

    /**
     * Map of nav button layout views to Enum type.
     * No synchronization is needed, and this is always accessed from the main thread.
     */
    private static final Map<View, NavigationButton> viewToButtonMap = new WeakHashMap<>();

    static {
        // On app startup litho can start before the navigation bar is initialized.
        // Force it to wait until the nav bar is updated.
        createNavButtonLatch();
    }

    private static void createNavButtonLatch() {
        navButtonLatch = new CountDownLatch(1);
    }

    private static void releaseNavButtonLatch() {
        CountDownLatch latch = navButtonLatch;
        if (latch != null) {
            navButtonLatch = null;
            latch.countDown();
        }
    }

    private static void waitForNavButtonLatchIfNeeded() {
        CountDownLatch latch = navButtonLatch;
        if (latch == null) {
            return;
        }

        if (Utils.isCurrentlyOnMainThread()) {
            // The latch is released from the main thread, and waiting from the main thread will always timeout.
            // This situation has only been observed when navigating out of a submenu and not changing tabs.
            // and for that use case the nav bar does not change so it's safe to return here.
            Logger.printDebug(() -> "Cannot block main thread waiting for nav button. " +
                    "Using last known navbar button status.");
            return;
        }

        try {
            Logger.printDebug(() -> "Latch wait started");
            if (latch.await(LATCH_AWAIT_TIMEOUT_MILLISECONDS, TimeUnit.MILLISECONDS)) {
                // Back button changed the navigation tab.
                Logger.printDebug(() -> "Latch wait complete");
                return;
            }

            // Timeout occurred, and a normal event when pressing the physical back button
            // does not change navigation tabs.
            releaseNavButtonLatch(); // Prevent other threads from waiting for no reason.
            Logger.printDebug(() -> "Latch wait timed out");

        } catch (InterruptedException ex) {
            // Calling YouTube thread was interrupted.
            Logger.printException(() -> "Latch wait interrupted", ex);
            Thread.currentThread().interrupt(); // Restore interrupt status flag.
        }
    }

    /**
     * Last YT navigation enum loaded.  Not necessarily the active navigation tab.
     * Always accessed from the main thread.
     */
    @Nullable
    private static String lastYTNavigationEnumName;

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

            for (NavigationButton buttonType : NavigationButton.values()) {
                if (buttonType.ytEnumNames.contains(lastEnumName)) {
                    Logger.printDebug(() -> "navigationTabLoaded: " + lastEnumName);
                    viewToButtonMap.put(navigationButtonGroup, buttonType);
                    navigationTabCreatedCallback(buttonType, navigationButtonGroup);
                    return;
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
        if (CREATE.ytEnumNames.contains(lastYTNavigationEnumName)) {
            navigationTabLoaded(view);
        } else {
            lastYTNavigationEnumName = NavigationButton.LIBRARY.ytEnumNames.get(0);
            navigationTabLoaded(view);
        }
    }

    /**
     * Injection point.
     */
    public static void navigationTabSelected(View navButtonImageView, boolean isSelected) {
        try {
            if (!isSelected) {
                return;
            }

            NavigationButton button = viewToButtonMap.get(navButtonImageView);

            if (button == null) { // An unknown tab was selected.
                // Show a toast only if debug mode is enabled.
                if (BaseSettings.DEBUG.get()) {
                    Logger.printException(() -> "Unknown navigation view selected: " + navButtonImageView);
                }

                NavigationButton.selectedNavigationButton = null;
                return;
            }

            NavigationButton.selectedNavigationButton = button;
            Logger.printDebug(() -> "Changed to navigation button: " + button);

            // Release any threads waiting for the selected nav button.
            releaseNavButtonLatch();
        } catch (Exception ex) {
            Logger.printException(() -> "navigationTabSelected failure", ex);
        }
    }

    /**
     * Injection point.
     */
    public static void onBackPressed(Activity activity) {
        Logger.printDebug(() -> "Back button pressed");
        createNavButtonLatch();
    }

    /** @noinspection EmptyMethod*/
    private static void navigationTabCreatedCallback(NavigationButton button, View tabView) {
        // Code is added during patching.
    }

    /**
     * Use the bundled non cairo filled icon instead of a custom icon.
     * Use the old non cairo filled icon, which is almost identical to
     * the what would be the filled cairo icon.
     */
    private static final int fillBellCairoBlack = Utils.getResourceIdentifier(
            "yt_fill_bell_black_24", "drawable");

    /**
     * Injection point.
     * Fixes missing drawable.
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void setCairoNotificationFilledIcon(EnumMap enumMap, Enum tabActivityCairo) {
        if (fillBellCairoBlack != 0) {
            // Show a popup informing this fix is no longer needed to those who might care.
            if (BaseSettings.DEBUG.get() && enumMap.containsKey(tabActivityCairo)) {
                Logger.printException(() -> "YouTube fixed the cairo notification icons");
            }
            enumMap.putIfAbsent(tabActivityCairo, fillBellCairoBlack);
        }
    }

    public enum NavigationButton {
        HOME("PIVOT_HOME", "TAB_HOME_CAIRO"),
        SHORTS("TAB_SHORTS", "TAB_SHORTS_CAIRO"),
        /**
         * Create new video tab.
         * This tab will never be in a selected state, even if the create video UI is on screen.
         */
        CREATE("CREATION_TAB_LARGE", "CREATION_TAB_LARGE_CAIRO"),
        /**
         * Only shown to automotive layout.
         */
        EXPLORE("TAB_EXPLORE"),
        SUBSCRIPTIONS("PIVOT_SUBSCRIPTIONS", "TAB_SUBSCRIPTIONS_CAIRO"),
        /**
         * Notifications tab.  Only present when
         * {@link Settings#SWITCH_CREATE_WITH_NOTIFICATIONS_BUTTON} is active.
         */
        NOTIFICATIONS("TAB_ACTIVITY", "TAB_ACTIVITY_CAIRO"),
        /**
         * Library tab, including if the user is in incognito mode or when logged out.
         */
        LIBRARY(
                // Modern library tab with 'You' layout.
                // The hooked YT code does not use an enum, and a dummy name is used here.
                "YOU_LIBRARY_DUMMY_PLACEHOLDER_NAME",
                // User is logged out.
                "ACCOUNT_CIRCLE",
                "ACCOUNT_CIRCLE_CAIRO",
                // User is logged in with incognito mode enabled.
                "INCOGNITO_CIRCLE",
                "INCOGNITO_CAIRO",
                // Old library tab (pre 'You' layout), only present when version spoofing.
                "VIDEO_LIBRARY_WHITE",
                // 'You' library tab that is sometimes momentarily loaded.
                // This might be a temporary tab while the user profile photo is loading,
                // but its exact purpose is not entirely clear.
                "PIVOT_LIBRARY"
        );

        @Nullable
        private static volatile NavigationButton selectedNavigationButton;

        /**
         * This will return null only if the currently selected tab is unknown.
         * This scenario will only happen if the UI has different tabs due to an A/B user test
         * or YT abruptly changes the navigation layout for some other reason.
         *
         * All code calling this method should handle a null return value.
         *
         * <b>Due to issues with how YT processes physical back button/gesture events,
         * this patch uses workarounds that can cause this method to take up to 120ms
         * if the device back button was recently pressed.</b>
         *
         * @return The active navigation tab.
         *         If the user is in the upload video UI, this returns tab that is still visually
         *         selected on screen (whatever tab the user was on before tapping the upload button).
         */
        @Nullable
        public static NavigationButton getSelectedNavigationButton() {
            waitForNavButtonLatchIfNeeded();
            return selectedNavigationButton;
        }

        /**
         * YouTube enum name for this tab.
         */
        private final List<String> ytEnumNames;

        NavigationButton(String... ytEnumNames) {
            this.ytEnumNames = Arrays.asList(ytEnumNames);
        }
    }
}
