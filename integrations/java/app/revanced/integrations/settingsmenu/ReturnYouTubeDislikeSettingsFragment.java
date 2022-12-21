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
import app.revanced.integrations.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
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

        // RYD API connection statistics
        
        if (SettingsEnum.DEBUG.getBoolean()) {
            PreferenceCategory emptyCategory = new PreferenceCategory(context); // vertical padding
            preferenceScreen.addPreference(emptyCategory);

            PreferenceCategory statisticsCategory = new PreferenceCategory(context);
            statisticsCategory.setTitle(str("revanced_ryd_statistics_category_title"));
            preferenceScreen.addPreference(statisticsCategory);

            Preference statisticPreference;

            statisticPreference = new Preference(context);
            statisticPreference.setSelectable(false);
            statisticPreference.setTitle(str("revanced_ryd_statistics_getFetchCallResponseTimeAverage_title"));
            statisticPreference.setSummary(createMillisecondStringFromNumber(ReturnYouTubeDislikeApi.getFetchCallResponseTimeAverage()));
            preferenceScreen.addPreference(statisticPreference);

            statisticPreference = new Preference(context);
            statisticPreference.setSelectable(false);
            statisticPreference.setTitle(str("revanced_ryd_statistics_getFetchCallResponseTimeMin_title"));
            statisticPreference.setSummary(createMillisecondStringFromNumber(ReturnYouTubeDislikeApi.getFetchCallResponseTimeMin()));
            preferenceScreen.addPreference(statisticPreference);

            statisticPreference = new Preference(context);
            statisticPreference.setSelectable(false);
            statisticPreference.setTitle(str("revanced_ryd_statistics_getFetchCallResponseTimeMax_title"));
            statisticPreference.setSummary(createMillisecondStringFromNumber(ReturnYouTubeDislikeApi.getFetchCallResponseTimeMax()));
            preferenceScreen.addPreference(statisticPreference);

            String fetchCallTimeWaitingLastSummary;
            final long fetchCallTimeWaitingLast = ReturnYouTubeDislikeApi.getFetchCallResponseTimeLast();
            if (fetchCallTimeWaitingLast == ReturnYouTubeDislikeApi.FETCH_CALL_RESPONSE_TIME_VALUE_RATE_LIMIT) {
                fetchCallTimeWaitingLastSummary = str("revanced_ryd_statistics_getFetchCallResponseTimeLast_rate_limit_summary");
            } else {
                fetchCallTimeWaitingLastSummary = createMillisecondStringFromNumber(fetchCallTimeWaitingLast);
            }
            statisticPreference = new Preference(context);
            statisticPreference.setSelectable(false);
            statisticPreference.setTitle(str("revanced_ryd_statistics_getFetchCallResponseTimeLast_title"));
            statisticPreference.setSummary(fetchCallTimeWaitingLastSummary);
            preferenceScreen.addPreference(statisticPreference);

            statisticPreference = new Preference(context);
            statisticPreference.setSelectable(false);
            statisticPreference.setTitle(str("revanced_ryd_statistics_getFetchCallCount_title"));
            statisticPreference.setSummary(createSummaryText(ReturnYouTubeDislikeApi.getFetchCallCount(),
                    "revanced_ryd_statistics_getFetchCallCount_zero_summary",
                    "revanced_ryd_statistics_getFetchCallCount_non_zero_summary"));
            preferenceScreen.addPreference(statisticPreference);

            statisticPreference = new Preference(context);
            statisticPreference.setSelectable(false);
            statisticPreference.setTitle(str("revanced_ryd_statistics_getFetchCallNumberOfFailures_title"));
            statisticPreference.setSummary(createSummaryText(ReturnYouTubeDislikeApi.getFetchCallNumberOfFailures(),
                    "revanced_ryd_statistics_getFetchCallNumberOfFailures_zero_summary",
                    "revanced_ryd_statistics_getFetchCallNumberOfFailures_non_zero_summary"));
            preferenceScreen.addPreference(statisticPreference);

            statisticPreference = new Preference(context);
            statisticPreference.setSelectable(false);
            statisticPreference.setTitle(str("revanced_ryd_statistics_getNumberOfRateLimitRequestsEncountered_title"));
            statisticPreference.setSummary(createSummaryText(ReturnYouTubeDislikeApi.getNumberOfRateLimitRequestsEncountered(),
                    "revanced_ryd_statistics_getNumberOfRateLimitRequestsEncountered_zero_summary",
                    "revanced_ryd_statistics_getNumberOfRateLimitRequestsEncountered_non_zero_summary"));
            preferenceScreen.addPreference(statisticPreference);
        }
    }

    private String createSummaryText(int value, String summaryStringZeroKey, String summaryStringOneOrMoreKey) {
        if (value == 0) {
            return str(summaryStringZeroKey);
        }
        return String.format(str(summaryStringOneOrMoreKey), value);
    }

    private static String createMillisecondStringFromNumber(long number) {
        return String.format(str("revanced_ryd_statistics_millisecond_text"), number);
    }

}
