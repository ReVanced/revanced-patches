package app.revanced.extension.music.settings.preference;

import android.widget.Toolbar;

import app.revanced.extension.music.settings.GoogleApiActivityHook;
import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.Utils;
import app.revanced.extension.shared.settings.preference.ToolbarPreferenceFragment;

/**
 * Preference fragment for ReVanced settings.
 */
@SuppressWarnings({"deprecation", "NewApi"})
public class ReVancedPreferenceFragment extends ToolbarPreferenceFragment {

    /**
     * Initializes the preference fragment.
     */
    @Override
    protected void initialize() {
        super.initialize();

        try {
            Utils.sortPreferenceGroups(getPreferenceScreen());
            setPreferenceScreenToolbar(getPreferenceScreen());
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }

    /**
     * Sets toolbar for all nested preference screens.
     */
    @Override
    protected void customizeToolbar(Toolbar toolbar) {
        GoogleApiActivityHook.setToolbarLayoutParams(toolbar);
    }
}
