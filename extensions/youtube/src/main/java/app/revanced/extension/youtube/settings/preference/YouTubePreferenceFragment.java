package app.revanced.extension.youtube.settings.preference;

import android.app.Dialog;
import android.preference.PreferenceScreen;
import android.widget.Toolbar;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ToolbarPreferenceFragment;
import app.revanced.extension.youtube.settings.YouTubeActivityHook;

/**
 * Preference fragment for ReVanced settings.
 */
@SuppressWarnings("deprecation")
public class YouTubePreferenceFragment extends ToolbarPreferenceFragment {
    /**
     * The main PreferenceScreen used to display the current set of preferences.
     */
    private PreferenceScreen preferenceScreen;

    /**
     * Initializes the preference fragment.
     */
    @Override
    protected void initialize() {
        super.initialize();

        try {
            preferenceScreen = getPreferenceScreen();
            Utils.sortPreferenceGroups(preferenceScreen);
            setPreferenceScreenToolbar(preferenceScreen);
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Called when the fragment starts.
     */
    @Override
    public void onStart() {
        super.onStart();
        try {
            // Initialize search controller if needed.
            if (YouTubeActivityHook.searchViewController != null) {
                // Trigger search data collection after fragment is ready.
                YouTubeActivityHook.searchViewController.initializeSearchData();
            }
        } catch (Exception ex) {
            Logger.printException(() -> "onStart failure", ex);
        }
    }

    /**
     * Sets toolbar for all nested preference screens.
     */
    @Override
    protected void customizeToolbar(Toolbar toolbar) {
        YouTubeActivityHook.setToolbarLayoutParams(toolbar);
    }

    /**
     * Perform actions after toolbar setup.
     */
    @Override
    protected void onPostToolbarSetup(Toolbar toolbar, Dialog preferenceScreenDialog) {
        if (YouTubeActivityHook.searchViewController != null
                && YouTubeActivityHook.searchViewController.isSearchActive()) {
            toolbar.post(() -> YouTubeActivityHook.searchViewController.closeSearch());
        }
    }

    /**
     * Returns the preference screen for external access by SearchViewController.
     */
    public PreferenceScreen getPreferenceScreenForSearch() {
        return preferenceScreen;
    }
}
