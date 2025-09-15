package app.revanced.extension.youtube.settings;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceFragment;
import android.view.View;
import android.widget.Toolbar;

import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.AppLanguage;
import app.revanced.extension.shared.settings.BaseActivityHook;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.youtube.patches.VersionCheckPatch;
import app.revanced.extension.youtube.patches.spoof.SpoofAppVersionPatch;
import app.revanced.extension.youtube.settings.preference.ReVancedPreferenceFragment;

/**
 * Hooks LicenseActivity to inject a custom ReVancedPreferenceFragment with a toolbar and search functionality.
 */
@SuppressWarnings("deprecation")
public class LicenseActivityHook extends BaseActivityHook {

    private static int currentThemeValueOrdinal = -1; // Must initially be a non-valid enum ordinal value.

    /**
     * Controller for managing search view components in the toolbar.
     */
    @SuppressLint("StaticFieldLeak")
    public static SearchViewController searchViewController;

    /**
     * Injection point
     * <p>
     * Creates an instance of LicenseActivityHook for use in static initialization.
     */
    @SuppressWarnings("unused")
    public static LicenseActivityHook createInstance() {
        return new LicenseActivityHook();
    }

    /**
     * Customizes the activity theme based on dark/light mode.
     */
    @Override
    protected void customizeActivityTheme(Activity activity) {
        final var theme = Utils.isDarkModeEnabled()
                ? "Theme.YouTube.Settings.Dark"
                : "Theme.YouTube.Settings";
        activity.setTheme(Utils.getResourceIdentifier(theme, "style"));
    }

    /**
     * Returns the resource ID for the YouTube settings layout.
     */
    @Override
    protected int getContentViewResourceId() {
        return Utils.getResourceIdentifier("revanced_settings_with_toolbar", "layout");
    }

    /**
     * Returns the toolbar background color based on dark/light mode.
     */
    @Override
    protected int getToolbarBackgroundColor() {
        final String colorName = Utils.isDarkModeEnabled()
                ? "yt_black3"
                : "yt_white1";
        return Utils.getColorFromString(colorName);
    }

    /**
     * Returns the navigation icon drawable for the toolbar.
     */
    @Override
    protected Drawable getNavigationIcon() {
        return ReVancedPreferenceFragment.getBackButtonDrawable();
    }

    /**
     * Returns the click listener for the navigation icon.
     */
    @Override
    protected View.OnClickListener getNavigationClickListener(Activity activity) {
        return null;
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
            searchViewController = SearchViewController.addSearchViewComponents(
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
     * Overrides the ReVanced settings language.
     */
    @SuppressWarnings("unused")
    public static Context getAttachBaseContext(Context original) {
        AppLanguage language = BaseSettings.REVANCED_LANGUAGE.get();
        if (language == AppLanguage.DEFAULT) {
            return original;
        }

        return Utils.getContext();
    }

    /**
     * Injection point.
     */
    @SuppressWarnings("unused")
    public static boolean useCairoSettingsFragment(boolean original) {
        // Early targets have layout issues and it's better to always force off.
        if (!VersionCheckPatch.IS_19_34_OR_GREATER) {
            return false;
        }
        if (Settings.RESTORE_OLD_SETTINGS_MENUS.get()) {
            return false;
        }
        // Spoofing can cause half broken settings menus of old and new settings.
        if (SpoofAppVersionPatch.isSpoofingToLessThan("19.35.36")) {
            return false;
        }

        // On the first launch of a clean install, forcing the cairo menu can give a
        // half broken appearance because all the preference icons may not be available yet.
        // 19.34+ cairo settings are always on, so it doesn't need to be forced anyway.
        // Cairo setting will show on the next launch of the app.
        return original;
    }

    /**
     * Injection point.
     * <p>
     * Updates dark/light mode since YT settings can force light/dark mode
     * which can differ from the global device settings.
     */
    @SuppressWarnings("unused")
    public static void updateLightDarkModeStatus(Enum<?> value) {
        final int themeOrdinal = value.ordinal();
        if (currentThemeValueOrdinal != themeOrdinal) {
            currentThemeValueOrdinal = themeOrdinal;
            Utils.setIsDarkModeEnabled(themeOrdinal == 1);
        }
    }

    /**
     * Handles configuration changes, such as orientation, to update the search view.
     */
    @SuppressWarnings("unused")
    public static void handleConfigurationChanged(Activity activity, Configuration newConfig) {
        if (searchViewController != null) {
            searchViewController.handleOrientationChange(newConfig.orientation);
        }
    }
}
