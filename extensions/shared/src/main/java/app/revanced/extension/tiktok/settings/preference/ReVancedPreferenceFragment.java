package app.revanced.extension.tiktok.settings.preference;

import android.preference.Preference;
import android.preference.PreferenceScreen;
import androidx.annotation.NonNull;
import app.revanced.extension.shared.settings.Setting;
import app.revanced.extension.shared.settings.preference.AbstractPreferenceFragment;
import app.revanced.extension.tiktok.settings.preference.categories.DownloadsPreferenceCategory;
import app.revanced.extension.tiktok.settings.preference.categories.ExtensionPreferenceCategory;
import app.revanced.extension.tiktok.settings.preference.categories.FeedFilterPreferenceCategory;
import app.revanced.extension.tiktok.settings.preference.categories.SimSpoofPreferenceCategory;
import org.jetbrains.annotations.NotNull;

/**
 * Preference fragment for ReVanced settings
 */
@SuppressWarnings("deprecation")
public class ReVancedPreferenceFragment extends AbstractPreferenceFragment {

    @Override
    protected void syncSettingWithPreference(@NonNull @NotNull Preference pref,
                                             @NonNull @NotNull Setting<?> setting,
                                             boolean applySettingToPreference) {
        if (pref instanceof RangeValuePreference) {
            RangeValuePreference rangeValuePref = (RangeValuePreference) pref;
            Setting.privateSetValueFromString(setting, rangeValuePref.getValue());
        } else if (pref instanceof DownloadPathPreference) {
            DownloadPathPreference downloadPathPref = (DownloadPathPreference) pref;
            Setting.privateSetValueFromString(setting, downloadPathPref.getValue());
        } else {
            super.syncSettingWithPreference(pref, setting, applySettingToPreference);
        }
    }

    @Override
    protected void initialize() {
        final var context = getContext();

        // Currently no resources can be compiled for TikTok (fails with aapt error).
        // So all TikTok Strings are hard coded in extension.
        restartDialogTitle = "Refresh and restart";
        restartDialogButtonText = "Restart";
        confirmDialogTitle = "Do you wish to proceed?";

        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        // Custom categories reference app specific Settings class.
        new FeedFilterPreferenceCategory(context, preferenceScreen);
        new DownloadsPreferenceCategory(context, preferenceScreen);
        new SimSpoofPreferenceCategory(context, preferenceScreen);
        new ExtensionPreferenceCategory(context, preferenceScreen);
    }
}
