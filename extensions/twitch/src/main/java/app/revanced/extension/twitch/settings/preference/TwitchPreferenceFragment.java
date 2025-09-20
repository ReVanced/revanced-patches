package app.revanced.extension.twitch.settings.preference;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.twitch.settings.Settings;

/**
 * Preference fragment for ReVanced settings.
 */
public class TwitchPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected void initialize() {
        super.initialize();

        // Do anything that forces this apps Settings bundle to load.
        if (Settings.BLOCK_VIDEO_ADS.get()) {
            Logger.printDebug(() -> "Block video ads enabled"); // Any statement that references the app settings.
        }
    }
}
