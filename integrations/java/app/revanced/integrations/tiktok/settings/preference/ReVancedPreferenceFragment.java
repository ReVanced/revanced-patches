package app.revanced.integrations.tiktok.settings.preference;

import android.preference.PreferenceScreen;
import app.revanced.integrations.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.integrations.tiktok.settings.preference.categories.DownloadsPreferenceCategory;
import app.revanced.integrations.tiktok.settings.preference.categories.FeedFilterPreferenceCategory;
import app.revanced.integrations.tiktok.settings.preference.categories.IntegrationsPreferenceCategory;
import app.revanced.integrations.tiktok.settings.preference.categories.SimSpoofPreferenceCategory;

/**
 * Preference fragment for ReVanced settings
 */
@SuppressWarnings("deprecation")
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected void initialize() {
        final var context = getContext();

        // Currently no resources can be compiled for TikTok (fails with aapt error).
        // So all TikTok Strings are hard coded in integrations.
        restartDialogTitle = "Refresh and restart";
        restartDialogButtonText = "Restart";
        confirmDialogTitle = "Do you wish to proceed?";

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        // Custom categories reference app specific Settings class.
        new FeedFilterPreferenceCategory(context, preferenceScreen);
        new DownloadsPreferenceCategory(context, preferenceScreen);
        new SimSpoofPreferenceCategory(context, preferenceScreen);
        new IntegrationsPreferenceCategory(context, preferenceScreen);
    }
}
