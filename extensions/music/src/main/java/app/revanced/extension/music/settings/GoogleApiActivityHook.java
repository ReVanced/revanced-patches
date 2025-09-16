package app.revanced.extension.music.settings;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceFragment;
import android.view.View;

import app.revanced.extension.music.settings.preference.ReVancedPreferenceFragment;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseActivityHook;

/**
 * Hooks GoogleApiActivity to inject a custom ReVancedPreferenceFragment with a toolbar.
 */
public class GoogleApiActivityHook extends BaseActivityHook {
    /**
     * Injection point
     * <p>
     * Creates an instance of GoogleApiActivityHook for use in static initialization.
     */
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
        return Utils.getResourceIdentifier("revanced_music_settings_with_toolbar", "layout");
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
        return view -> activity.finish();
    }

    /**
     * Creates a new ReVancedPreferenceFragment for the activity.
     */
    @Override
    protected PreferenceFragment createPreferenceFragment() {
        return new ReVancedPreferenceFragment();
    }
}
