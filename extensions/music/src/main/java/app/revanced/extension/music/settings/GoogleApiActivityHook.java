package app.revanced.extension.music.settings;

import android.app.Activity;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceFragment;
import android.view.View;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.BaseActivityHook;
import app.revanced.extension.music.settings.preference.ReVancedPreferenceFragment;

/**
 * Hooks GoogleApiActivity to inject a custom ReVancedPreferenceFragment with a toolbar.
 */
@SuppressWarnings("deprecation")
public class GoogleApiActivityHook extends BaseActivityHook {
    /**
     * Creates an instance of GoogleApiActivityHook for use in static initialization.
     */
    public static GoogleApiActivityHook createInstance() {
        return new GoogleApiActivityHook();
    }

    /**
     * Sets the fixed theme for the activity.
     */
    @Override
    protected void customizeActivityTheme(Activity activity) {
        activity.setTheme(Utils.getResourceIdentifier("Base.Theme.YouTubeMusic", "style"));
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
