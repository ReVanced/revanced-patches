package app.revanced.integrations.youtube.settings.preference;

import android.preference.ListPreference;
import android.preference.Preference;
import app.revanced.integrations.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.integrations.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.integrations.youtube.settings.Settings;

/**
 * Preference fragment for ReVanced settings.
 *
 * @noinspection deprecation
 */
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {
    @Override
    protected void initialize() {
        super.initialize();

        // If the preference was included, then initialize it based on the available playback speed
        Preference defaultSpeedPreference = findPreference(Settings.PLAYBACK_SPEED_DEFAULT.key);
        if (defaultSpeedPreference instanceof ListPreference) {
            CustomPlaybackSpeedPatch.initializeListPreference((ListPreference) defaultSpeedPreference);
        }
    }
}
