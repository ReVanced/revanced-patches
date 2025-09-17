package app.revanced.extension.music.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.Toolbar;

import app.revanced.extension.music.settings.preference.ReVancedPreferenceFragment;
import app.revanced.extension.music.settings.search.MusicSearchViewController;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseActivityHook;

/**
 * Hooks GoogleApiActivity to inject a custom ReVancedPreferenceFragment with a toolbar and search.
 */
@SuppressWarnings("deprecation")
public class GoogleApiActivityHook extends BaseActivityHook {

    @SuppressLint("StaticFieldLeak")
    public static MusicSearchViewController searchViewController;

    @SuppressWarnings("unused")
    public static GoogleApiActivityHook createInstance() {
        // Must touch the Music settings to ensure the class is loaded and
        // the values can be found when setting the UI preferences.
        // Logging anything under non debug ensures this is set.
        Logger.printInfo(() -> "Permanent repeat enabled: " + Settings.PERMANENT_REPEAT.get());

        // YT Music always uses dark mode.
        Utils.setIsDarkModeEnabled(true);

        return new GoogleApiActivityHook();
    }

    /**
     * Sets the fixed theme for the activity.
     */
    @Override
    protected void customizeActivityTheme(Activity activity) {
        // Override the default YouTube Music theme to increase start padding of list items.
        // Custom style located in resources/music/values/style.xml
        activity.setTheme(Utils.getResourceIdentifier("Theme.ReVanced.YouTubeMusic.Settings", "style"));
    }

    /**
     * Returns the resource ID for the YouTube Music settings layout.
     */
    @Override
    protected int getContentViewResourceId() {
        return LAYOUT_REVANCED_SETTINGS_WITH_TOOLBAR;
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
        Drawable navigationIcon = ReVancedPreferenceFragment.getBackButtonDrawable();
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
     * Adds search view components to the toolbar for ReVancedPreferenceFragment.
     *
     * @param activity The activity hosting the toolbar.
     * @param toolbar  The configured toolbar.
     * @param fragment The PreferenceFragment associated with the activity.
     */
    @Override
    protected void onPostToolbarSetup(Activity activity, Toolbar toolbar, PreferenceFragment fragment) {
        if (fragment instanceof ReVancedPreferenceFragment) {
            searchViewController = MusicSearchViewController.addSearchViewComponents(
                    activity, toolbar, (ReVancedPreferenceFragment) fragment);
        }
    }

    /**
     * Creates a new ReVancedPreferenceFragment for the activity.
     */
    @Override
    protected PreferenceFragment createPreferenceFragment() {
        return new ReVancedPreferenceFragment();
    }

    /**
     * Injection point.
     * <p>
     * Static method for back press handling.
     */
    @SuppressWarnings("unused")
    public static boolean handleBackPress() {
        return MusicSearchViewController.handleBackPress(searchViewController);
    }
}
