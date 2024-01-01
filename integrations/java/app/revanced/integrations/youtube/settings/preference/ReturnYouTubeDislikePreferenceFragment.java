package app.revanced.integrations.youtube.settings.preference;

import static app.revanced.integrations.shared.StringRef.str;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;

import app.revanced.integrations.shared.Logger;
import app.revanced.integrations.shared.settings.Setting;
import app.revanced.integrations.shared.settings.BaseSettings;
import app.revanced.integrations.youtube.patches.ReturnYouTubeDislikePatch;
import app.revanced.integrations.youtube.returnyoutubedislike.ReturnYouTubeDislike;
import app.revanced.integrations.youtube.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;
import app.revanced.integrations.youtube.settings.Settings;

/** @noinspection deprecation*/
public class ReturnYouTubeDislikePreferenceFragment extends PreferenceFragment {

    /**
     * If dislikes are shown on Shorts.
     */
    private SwitchPreference shortsPreference;

    /**
     * If dislikes are shown as percentage.
     */
    private SwitchPreference percentagePreference;

    /**
     * If segmented like/dislike button uses smaller compact layout.
     */
    private SwitchPreference compactLayoutPreference;

    /**
     * If segmented like/dislike button uses smaller compact layout.
     */
    private SwitchPreference toastOnRYDNotAvailable;

    private void updateUIState() {
        shortsPreference.setEnabled(Settings.RYD_SHORTS.isAvailable());
        percentagePreference.setEnabled(Settings.RYD_DISLIKE_PERCENTAGE.isAvailable());
        compactLayoutPreference.setEnabled(Settings.RYD_COMPACT_LAYOUT.isAvailable());
        toastOnRYDNotAvailable.setEnabled(Settings.RYD_TOAST_ON_CONNECTION_ERROR.isAvailable());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            Activity context = getActivity();
            PreferenceManager manager = getPreferenceManager();
            manager.setSharedPreferencesName(Setting.preferences.name);
            PreferenceScreen preferenceScreen = manager.createPreferenceScreen(context);
            setPreferenceScreen(preferenceScreen);

            SwitchPreference enabledPreference = new SwitchPreference(context);
            enabledPreference.setChecked(Settings.RYD_ENABLED.get());
            enabledPreference.setTitle(str("revanced_ryd_enable_title"));
            enabledPreference.setSummaryOn(str("revanced_ryd_enable_summary_on"));
            enabledPreference.setSummaryOff(str("revanced_ryd_enable_summary_off"));
            enabledPreference.setOnPreferenceChangeListener((pref, newValue) -> {
                final Boolean rydIsEnabled = (Boolean) newValue;
                Settings.RYD_ENABLED.save(rydIsEnabled);
                ReturnYouTubeDislikePatch.onRYDStatusChange(rydIsEnabled);

                updateUIState();
                return true;
            });
            preferenceScreen.addPreference(enabledPreference);

            shortsPreference = new SwitchPreference(context);
            shortsPreference.setChecked(Settings.RYD_SHORTS.get());
            shortsPreference.setTitle(str("revanced_ryd_shorts_title"));
            String shortsSummary = str("revanced_ryd_shorts_summary_on",
                    ReturnYouTubeDislikePatch.IS_SPOOFING_TO_NON_LITHO_SHORTS_PLAYER
                            ? ""
                            : "\n\n" + str("revanced_ryd_shorts_summary_disclaimer"));
            shortsPreference.setSummaryOn(shortsSummary);
            shortsPreference.setSummaryOff(str("revanced_ryd_shorts_summary_off"));
            shortsPreference.setOnPreferenceChangeListener((pref, newValue) -> {
                Settings.RYD_SHORTS.save((Boolean) newValue);
                updateUIState();
                return true;
            });
            preferenceScreen.addPreference(shortsPreference);

            percentagePreference = new SwitchPreference(context);
            percentagePreference.setChecked(Settings.RYD_DISLIKE_PERCENTAGE.get());
            percentagePreference.setTitle(str("revanced_ryd_dislike_percentage_title"));
            percentagePreference.setSummaryOn(str("revanced_ryd_dislike_percentage_summary_on"));
            percentagePreference.setSummaryOff(str("revanced_ryd_dislike_percentage_summary_off"));
            percentagePreference.setOnPreferenceChangeListener((pref, newValue) -> {
                Settings.RYD_DISLIKE_PERCENTAGE.save((Boolean) newValue);
                ReturnYouTubeDislike.clearAllUICaches();
                updateUIState();
                return true;
            });
            preferenceScreen.addPreference(percentagePreference);

            compactLayoutPreference = new SwitchPreference(context);
            compactLayoutPreference.setChecked(Settings.RYD_COMPACT_LAYOUT.get());
            compactLayoutPreference.setTitle(str("revanced_ryd_compact_layout_title"));
            compactLayoutPreference.setSummaryOn(str("revanced_ryd_compact_layout_summary_on"));
            compactLayoutPreference.setSummaryOff(str("revanced_ryd_compact_layout_summary_off"));
            compactLayoutPreference.setOnPreferenceChangeListener((pref, newValue) -> {
                Settings.RYD_COMPACT_LAYOUT.save((Boolean) newValue);
                ReturnYouTubeDislike.clearAllUICaches();
                updateUIState();
                return true;
            });
            preferenceScreen.addPreference(compactLayoutPreference);

            toastOnRYDNotAvailable = new SwitchPreference(context);
            toastOnRYDNotAvailable.setChecked(Settings.RYD_TOAST_ON_CONNECTION_ERROR.get());
            toastOnRYDNotAvailable.setTitle(str("ryd_toast_on_connection_error_title"));
            toastOnRYDNotAvailable.setSummaryOn(str("ryd_toast_on_connection_error_summary_on"));
            toastOnRYDNotAvailable.setSummaryOff(str("ryd_toast_on_connection_error_summary_off"));
            toastOnRYDNotAvailable.setOnPreferenceChangeListener((pref, newValue) -> {
                Settings.RYD_TOAST_ON_CONNECTION_ERROR.save((Boolean) newValue);
                updateUIState();
                return true;
            });
            preferenceScreen.addPreference(toastOnRYDNotAvailable);

            updateUIState();


            // About category

            PreferenceCategory aboutCategory = new PreferenceCategory(context);
            aboutCategory.setTitle(str("revanced_ryd_about"));
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

            if (BaseSettings.DEBUG.get()) {
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
        } catch (Exception ex) {
            Logger.printException(() -> "onCreate failure", ex);
        }
    }

    private static String createSummaryText(int value, String summaryStringZeroKey, String summaryStringOneOrMoreKey) {
        if (value == 0) {
            return str(summaryStringZeroKey);
        }
        return String.format(str(summaryStringOneOrMoreKey), value);
    }

    private static String createMillisecondStringFromNumber(long number) {
        return String.format(str("revanced_ryd_statistics_millisecond_text"), number);
    }

}
