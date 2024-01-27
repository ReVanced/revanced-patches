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

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        // Custom categories reference app specific Settings class.
        new FeedFilterPreferenceCategory(context, preferenceScreen);
        new DownloadsPreferenceCategory(context, preferenceScreen);
        new SimSpoofPreferenceCategory(context, preferenceScreen);
        new IntegrationsPreferenceCategory(context, preferenceScreen);
    }
}
