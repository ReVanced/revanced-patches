package app.revanced.integrations.twitch.settings.preference;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.integrations.twitch.settings.Settings;

/**
 * Preference fragment for ReVanced settings
 */
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected void initialize() {
        super.initialize();

        // Do anything that forces this apps Settings bundle to load.
        if (Settings.BLOCK_VIDEO_ADS.get()) {
            Logger.printDebug(() -> "Block video ads enabled"); // Any statement that references the app settings.
        }
    }
}
