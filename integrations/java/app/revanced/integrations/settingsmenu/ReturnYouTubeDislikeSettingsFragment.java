package app.revanced.integrations.settingsmenu;

import static app.revanced.integrations.sponsorblock.StringRef.str;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import app.revanced.integrations.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.settings.SettingsEnum;
import app.revanced.integrations.utils.SharedPrefHelper;

public class ReturnYouTubeDislikeSettingsFragment extends PreferenceFragment {

    /**
     * If ReturnYouTubeDislike is enabled
     */
    private SwitchPreference enabledPreference;

    /**
     * If dislikes are shown as percentage
     */
    private SwitchPreference percentagePreference;

    private void updateUIState() {
        final boolean rydIsEnabled = SettingsEnum.RYD_ENABLED.getBoolean();
        final boolean dislikePercentageEnabled = SettingsEnum.RYD_SHOW_DISLIKE_PERCENTAGE.getBoolean();

        enabledPreference.setSummary(rydIsEnabled
                ? str("revanced_ryd_enable_summary_on")
                : str("revanced_ryd_enable_summary_off"));

        percentagePreference.setSummary(dislikePercentageEnabled
                ? str("revanced_ryd_dislike_percentage_summary_on")
                : str("revanced_ryd_dislike_percentage_summary_off"));
        percentagePreference.setEnabled(rydIsEnabled);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName(SharedPrefHelper.SharedPrefNames.RYD.getName());

        Activity context = this.getActivity();
        PreferenceScreen preferenceScreen = getPreferenceManager().createPreferenceScreen(context);
        setPreferenceScreen(preferenceScreen);

        enabledPreference = new SwitchPreference(context);
        enabledPreference.setKey(SettingsEnum.RYD_ENABLED.getPath());
        enabledPreference.setDefaultValue(SettingsEnum.RYD_ENABLED.getDefaultValue());
        enabledPreference.setChecked(SettingsEnum.RYD_ENABLED.getBoolean());
        enabledPreference.setTitle(str("revanced_ryd_enable_title"));
        enabledPreference.setOnPreferenceChangeListener((pref, newValue) -> {
            final boolean rydIsEnabled = (Boolean) newValue;
            SettingsEnum.RYD_ENABLED.saveValue(rydIsEnabled);
            ReturnYouTubeDislike.onEnabledChange(rydIsEnabled);

            updateUIState();
            return true;
        });
        preferenceScreen.addPreference(enabledPreference);

        percentagePreference = new SwitchPreference(context);
        percentagePreference.setKey(SettingsEnum.RYD_SHOW_DISLIKE_PERCENTAGE.getPath());
        percentagePreference.setDefaultValue(SettingsEnum.RYD_SHOW_DISLIKE_PERCENTAGE.getDefaultValue());
        percentagePreference.setChecked(SettingsEnum.RYD_SHOW_DISLIKE_PERCENTAGE.getBoolean());
        percentagePreference.setTitle(str("revanced_ryd_dislike_percentage_title"));
        percentagePreference.setOnPreferenceChangeListener((pref, newValue) -> {
            SettingsEnum.RYD_SHOW_DISLIKE_PERCENTAGE.saveValue((Boolean)newValue);

            updateUIState();
            return true;
        });
        preferenceScreen.addPreference(percentagePreference);

        updateUIState();


        // About category

        PreferenceCategory aboutCategory = new PreferenceCategory(context);
        aboutCategory.setTitle(str("about"));
        preferenceScreen.addPreference(aboutCategory);

        // ReturnYouTubeDislike Website

        Preference aboutWebsitePreference = new Preference(context);
        aboutWebsitePreference.setTitle(str("revanced_ryd_attribution_title"));
        aboutWebsitePreference.setSummary(str("revanced_ryd_attribution_summary"));
        aboutWebsitePreference.setOnPreferenceClickListener(pref -> {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://returnyoutubedislike.com"));
            pref.getContext().startActivity(i);
            return false;
        });
        preferenceScreen.addPreference(aboutWebsitePreference);
    }

}
