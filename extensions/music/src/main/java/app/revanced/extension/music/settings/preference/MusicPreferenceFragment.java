package app.revanced.extension.music.settings.preference;

import android.app.Dialog;
import android.preference.PreferenceScreen;
import android.widget.Toolbar;

import app.revanced.extension.music.settings.MusicActivityHook;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ToolbarPreferenceFragment;

/**
 * Preference fragment for ReVanced settings.
 */
@SuppressWarnings("deprecation")
public class MusicPreferenceFragment extends ToolbarPreferenceFragment {
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
            // Initialize search controller if needed
            if (MusicActivityHook.searchViewController != null) {
                // Trigger search data collection after fragment is ready.
                MusicActivityHook.searchViewController.initializeSearchData();
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
        MusicActivityHook.setToolbarLayoutParams(toolbar);
    }

    /**
     * Perform actions after toolbar setup.
     */
    @Override
    protected void onPostToolbarSetup(Toolbar toolbar, Dialog preferenceScreenDialog) {
        if (MusicActivityHook.searchViewController != null
                && MusicActivityHook.searchViewController.isSearchActive()) {
            toolbar.post(() -> MusicActivityHook.searchViewController.closeSearch());
        }
    }

    /**
     * Returns the preference screen for external access by SearchViewController.
     */
    public PreferenceScreen getPreferenceScreenForSearch() {
        return preferenceScreen;
    }
}
