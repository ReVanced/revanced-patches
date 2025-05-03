package app.revanced.extension.youtube.returnyoutubedislike.ui;

import static app.revanced.extension.shared.StringRef.str;

import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import app.revanced.extension.shared.Logger;
import app.revanced.extension.shared.settings.BaseSettings;
import app.revanced.extension.youtube.returnyoutubedislike.requests.ReturnYouTubeDislikeApi;

@SuppressWarnings({"unused", "deprecation"})
public class ReturnYouTubeDislikeDebugStatsPreferenceCategory extends PreferenceCategory {

    private static final boolean SHOW_RYD_DEBUG_STATS = BaseSettings.DEBUG.get();

    private static String createSummaryText(int value, String summaryStringZeroKey, String summaryStringOneOrMoreKey) {
        if (value == 0) {
            return str(summaryStringZeroKey);
        }
        return str(summaryStringOneOrMoreKey, value);
    }

    private static String createMillisecondStringFromNumber(long number) {
        return String.format(str("revanced_ryd_statistics_millisecond_text"), number);
    }

    public ReturnYouTubeDislikeDebugStatsPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReturnYouTubeDislikeDebugStatsPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ReturnYouTubeDislikeDebugStatsPreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected View onCreateView(ViewGroup parent) {
        if (!SHOW_RYD_DEBUG_STATS) {
            // Use an empty view to hide without removing.
            return new View(getContext());
        }

        return super.onCreateView(parent);
    }

    protected void onAttachedToActivity() {
        try {
            super.onAttachedToActivity();
            if (!SHOW_RYD_DEBUG_STATS) {
                return;
            }

            Logger.printDebug(() -> "Updating stats preferences");
            removeAll();

            addStatisticPreference(
                    "revanced_ryd_statistics_getFetchCallResponseTimeAverage_title",
                    createMillisecondStringFromNumber(ReturnYouTubeDislikeApi.getFetchCallResponseTimeAverage())
            );

            addStatisticPreference(
                    "revanced_ryd_statistics_getFetchCallResponseTimeMin_title",
                    createMillisecondStringFromNumber(ReturnYouTubeDislikeApi.getFetchCallResponseTimeMin())
            );

            addStatisticPreference(
                    "revanced_ryd_statistics_getFetchCallResponseTimeMax_title",
                    createMillisecondStringFromNumber(ReturnYouTubeDislikeApi.getFetchCallResponseTimeMax())
            );

            String fetchCallTimeWaitingLastSummary;
            final long fetchCallTimeWaitingLast = ReturnYouTubeDislikeApi.getFetchCallResponseTimeLast();
            if (fetchCallTimeWaitingLast == ReturnYouTubeDislikeApi.FETCH_CALL_RESPONSE_TIME_VALUE_RATE_LIMIT) {
                fetchCallTimeWaitingLastSummary = str("revanced_ryd_statistics_getFetchCallResponseTimeLast_rate_limit_summary");
            } else {
                fetchCallTimeWaitingLastSummary = createMillisecondStringFromNumber(fetchCallTimeWaitingLast);
            }
            addStatisticPreference(
                    "revanced_ryd_statistics_getFetchCallResponseTimeLast_title",
                    fetchCallTimeWaitingLastSummary
            );

            addStatisticPreference(
                    "revanced_ryd_statistics_getFetchCallCount_title",
                    createSummaryText(ReturnYouTubeDislikeApi.getFetchCallCount(),
                            "revanced_ryd_statistics_getFetchCallCount_zero_summary",
                            "revanced_ryd_statistics_getFetchCallCount_non_zero_summary"
                    )
            );

            addStatisticPreference(
                    "revanced_ryd_statistics_getFetchCallNumberOfFailures_title",
                    createSummaryText(ReturnYouTubeDislikeApi.getFetchCallNumberOfFailures(),
                            "revanced_ryd_statistics_getFetchCallNumberOfFailures_zero_summary",
                            "revanced_ryd_statistics_getFetchCallNumberOfFailures_non_zero_summary"
                    )
            );

            addStatisticPreference(
                    "revanced_ryd_statistics_getNumberOfRateLimitRequestsEncountered_title",
                    createSummaryText(ReturnYouTubeDislikeApi.getNumberOfRateLimitRequestsEncountered(),
                            "revanced_ryd_statistics_getNumberOfRateLimitRequestsEncountered_zero_summary",
                            "revanced_ryd_statistics_getNumberOfRateLimitRequestsEncountered_non_zero_summary"
                    )
            );
        } catch (Exception ex) {
            Logger.printException(() -> "onAttachedToActivity failure", ex);
        }
    }

    private void addStatisticPreference(String titleKey, String SummaryText) {
        Preference statisticPreference = new Preference(getContext());
        statisticPreference.setSelectable(false);
        statisticPreference.setTitle(str(titleKey));
        statisticPreference.setSummary(SummaryText);
        addPreference(statisticPreference);
    }
}
