package app.revanced.extension.youtube.settings.preference;

import android.os.Build;
import android.preference.ListPreference;
import android.preference.Preference;

import androidx.annotation.RequiresApi;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.youtube.patches.playback.speed.CustomPlaybackSpeedPatch;
import app.revanced.extension.youtube.settings.Settings;

/**
 * Preference fragment for ReVanced settings.
 *
 * @noinspection deprecation
 */
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void initialize() {
        super.initialize();

        try {
            // If the preference was included, then initialize it based on the available playback speed.
            Preference defaultSpeedPreference = findPreference(Settings.PLAYBACK_SPEED_DEFAULT.key);
            if (defaultSpeedPreference instanceof ListPreference) {
                CustomPlaybackSpeedPatch.initializeListPreference((ListPreference) defaultSpeedPreference);
            }
        } catch (Exception ex) {
            Logger.printException(() -> "initialize failure", ex);
        }
    }
}
