package app.revanced.extension.music.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.Toolbar;

import app.revanced.extension.music.VersionCheckUtils;
import app.revanced.extension.music.settings.preference.MusicPreferenceFragment;
import app.revanced.extension.music.settings.search.MusicSearchViewController;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.ResourceType;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseActivityHook;

/**
 * Hooks GoogleApiActivity to inject a custom {@link MusicPreferenceFragment} with a toolbar and search.
 */
public class MusicActivityHook extends BaseActivityHook {

    @SuppressLint("StaticFieldLeak")
    public static MusicSearchViewController searchViewController;

    /**
     * How much time has passed since the first launch of the app. Simple check to prevent
     * forcing bold icons on first launch where the settings menu is partially broken
     * due to missing icon resources the client has not yet received.
     *
     * @see app.revanced.extension.youtube.settings.YouTubeActivityHook#MINIMUM_TIME_AFTER_FIRST_LAUNCH_BEFORE_ALLOWING_BOLD_ICONS
     */
    private static final long MINIMUM_TIME_AFTER_FIRST_LAUNCH_BEFORE_ALLOWING_BOLD_ICONS = 30 * 1000; // 30 seconds.

    static {
        final boolean useBoldIcons = VersionCheckUtils.IS_8_40_OR_GREATER
                && !Settings.SETTINGS_DISABLE_BOLD_ICONS.get()
                && (System.currentTimeMillis() - Settings.FIRST_TIME_APP_LAUNCHED.get())
                > MINIMUM_TIME_AFTER_FIRST_LAUNCH_BEFORE_ALLOWING_BOLD_ICONS;

        Utils.setAppIsUsingBoldIcons(useBoldIcons);
    }

    /**
     * Injection point.
     */
    @SuppressWarnings("unused")
    public static void initialize(Activity parentActivity) {
        // Must touch the Music settings to ensure the class is loaded and
        // the values can be found when setting the UI preferences.
        // Logging anything under non debug ensures this is set.
        Logger.printInfo(() -> "Permanent repeat enabled: " + Settings.PERMANENT_REPEAT.get());

        // YT Music always uses dark mode.
        Utils.setIsDarkModeEnabled(true);

        BaseActivityHook.initialize(new MusicActivityHook(), parentActivity);
    }

    /**
     * Sets the fixed theme for the activity.
     */
    @Override
    protected void customizeActivityTheme(Activity activity) {
        // Override the default YouTube Music theme to increase start padding of list items.
        // Custom style located in resources/music/values/style.xml
        activity.setTheme(Utils.getResourceIdentifierOrThrow(
                ResourceType.STYLE, "Theme.ReVanced.YouTubeMusic.Settings"));
    }

    /**
     * Returns the fixed background color for the toolbar.
     */
    @Override
    protected int getToolbarBackgroundColor() {
        return Utils.getResourceColor("ytm_color_black");
    }

    /**
     * Returns the navigation icon with a color filter applied.
     */
    @Override
    protected Drawable getNavigationIcon() {
        Drawable navigationIcon = MusicPreferenceFragment.getBackButtonDrawable();
        navigationIcon.setColorFilter(Utils.getAppForegroundColor(), PorterDuff.Mode.SRC_IN);
        return navigationIcon;
    }

    /**
     * Returns the click listener that finishes the activity when the navigation icon is clicked.
     */
    @Override
    protected View.OnClickListener getNavigationClickListener(Activity activity) {
        return view -> {
            if (searchViewController != null && searchViewController.isSearchActive()) {
                searchViewController.closeSearch();
            } else {
                activity.finish();
            }
        };
    }

    /**
     * Adds search view components to the toolbar for {@link MusicPreferenceFragment}.
     *
     * @param activity The activity hosting the toolbar.
     * @param toolbar  The configured toolbar.
     * @param fragment The PreferenceFragment associated with the activity.
     */
    @Override
    protected void onPostToolbarSetup(Activity activity, Toolbar toolbar, PreferenceFragment fragment) {
        if (fragment instanceof MusicPreferenceFragment) {
            searchViewController = MusicSearchViewController.addSearchViewComponents(
                    activity, toolbar, (MusicPreferenceFragment) fragment);
        }
    }

    /**
     * Creates a new {@link MusicPreferenceFragment} for the activity.
     */
    @Override
    protected PreferenceFragment createPreferenceFragment() {
        return new MusicPreferenceFragment();
    }

    /**
     * Injection point.
     * <p>
     * Overrides {@link Activity#finish()} of the injection Activity.
     *
     * @return if the original activity finish method should be allowed to run.
     */
    @SuppressWarnings("unused")
    public static boolean handleFinish() {
        return MusicSearchViewController.handleFinish(searchViewController);
    }

    /**
     * Injection point.
     * <p>
     * Decides whether to use bold icons.
     */
    @SuppressWarnings("unused")
    public static boolean useBoldIcons(boolean original) {
        return Utils.appIsUsingBoldIcons();
    }
}
